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

        startWebSocketServer();

        getServer().getPluginManager().registerEvents(new OmniSightEventListener(this), this);

        if (oshiAvailable) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    OshiProvider.updateMetrics();
                }
            }.runTaskTimerAsynchronously(this, 0L, 20L);
        }

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
        reloadConfig();
        setupConfig();

        StatusData.init(this);

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
            wsServer.updateSettings();
        }

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