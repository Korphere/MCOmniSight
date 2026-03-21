package com.korphere.mcomnisight;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import static com.korphere.mcomnisight.StatusData.oshiAvailable;
import static com.korphere.mcomnisight.StatusData.plugin;

public final class MCOmniSight extends JavaPlugin {

    private OmniSightServer wsServer;
    private BukkitRunnable updateTask;

    @Override
    public void onEnable() {
        setupConfig();

        StatusData.init(this);

        // サーバー起動
        startWebSocketServer();

        getServer().getPluginManager().registerEvents(new OmniSightEventListener(this), this);

        // メトリクス更新タスク (OSHI)
        if (oshiAvailable) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    OshiProvider.updateMetrics();
                }
            }.runTaskTimerAsynchronously(this, 0L, 20L);
        }

        // データ配信タスクの開始
        startUpdateTask();

        OmniSightCommand cmd = new OmniSightCommand();
        getCommand("omnisight").setExecutor(cmd);
        getCommand("omnisight").setTabCompleter(cmd);
    }

    private void startWebSocketServer() {
        int port = getConfig().getInt("websocket-port", 8887);
        wsServer = new OmniSightServer(port, plugin);
        wsServer.start();
        getLogger().info("WebSocket Server started on port: " + port);
    }

    private void startUpdateTask() {
        if (updateTask != null) updateTask.cancel();

        boolean useGzip = getConfig().getBoolean("gzip-enabled", true);
        int intervalTicks = getConfig().getInt("update-interval-ticks", 20);

        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (wsServer != null) {
                    StatusData.sendUpdate(wsServer, useGzip);
                }
            }
        };
        updateTask.runTaskTimer(this, 0L, intervalTicks);
    }

    public void reloadPlugin() {
        // 1. 物理ファイルからメモリへ最新設定を読み込む
        reloadConfig();
        // 2. 念のため、新しく追加されたデフォルト値があれば補完する
        setupConfig();

        // 3. 各プロバイダー（StatusData等）の静的キャッシュを更新
        // これをやらないと、内部で保持している「有効フラグ」が古いままになります
        StatusData.init(this);

        // 4. WebSocketサーバーの再起動判定
        int newPort = getConfig().getInt("websocket-port", 8887);
        if (wsServer == null || wsServer.getPort() != newPort) {
            getLogger().info("Port change detected. Restarting WebSocket server...");
            try {
                if (wsServer != null) wsServer.stop(1000);
                startWebSocketServer();
            } catch (InterruptedException e) {
                getLogger().severe("サーバーの再起動に失敗しました。");
            }
        } else {
            // ポートが変わっていない場合でも、新しい認証キーやホワイトリストを反映させる
            // ※ wsServer 側に updateSettings メソッドを作っておくとスマートです
            wsServer.updateSettings();
        }

        // 5. データ配信タスク（スケジュール）の再起動
        // これにより、update-interval-ticks の変更が即座に反映されます
        startUpdateTask();

        getLogger().info("Configuration and tasks have been re-synchronized.");
    }

    @Override
    public void onDisable() {
        if (wsServer != null) {
            try {
                wsServer.stop(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public OmniSightServer getWsServer() {
        return wsServer;
    }

    public void setupConfig() {
        saveDefaultConfig();

        getConfig().options().copyDefaults(true);
        saveConfig();
    }
}