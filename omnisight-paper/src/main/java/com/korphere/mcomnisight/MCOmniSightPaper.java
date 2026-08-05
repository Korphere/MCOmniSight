package com.korphere.mcomnisight;

import com.korphere.mcomnisight.listener.OmniSightEventListener;
import com.korphere.mcomnisight.network.InternalBridgeClient;
import com.korphere.mcomnisight.provider.MetricsRunner;
import com.korphere.mcomnisight.server.OmniSightServer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import static com.korphere.mcomnisight.StatusData.oshiAvailable;
import static com.korphere.mcomnisight.StatusData.plugin;

public final class MCOmniSightPaper extends JavaPlugin {

    private OmniSightServer wsServer;
    private BukkitRunnable updateTask;
    private InternalBridgeClient bridgeClient;
    private VConnection cmode;

    @Override
    public void onEnable() {
        setupConfig();

        StatusData.init(this);

        setupNetworkMode();

        getServer().getPluginManager().registerEvents(new OmniSightEventListener(this), this);

        if (oshiAvailable) {
            try {
                MetricsRunner runner = (MetricsRunner) Class.forName("com.korphere.mcomnisight.provider.OshiRunner")
                        .getDeclaredConstructor()
                        .newInstance();

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        runner.update();
                    }
                }.runTaskTimerAsynchronously(this, 0L, 20L);

            } catch (Exception e) {
                getLogger().warning("OSHI Init Failed: " + e.getMessage());
            }
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

    private void initProxySender() {
        bridgeClient = new InternalBridgeClient(MCOmniSightPaper.this);
    }

    private boolean connectProxy() {
        int port = getConfig().getInt("proxy.port", 8888);
        String host = getConfig().getString("proxy.host", "127.0.0.1");
        return bridgeClient.connect(host, port);
    }

    private void startUpdateTask() {
        if (updateTask != null) updateTask.cancel();

        boolean useGzip = getConfig().getBoolean("gzip-enabled", true);
        int intervalTicks = getConfig().getInt("update-interval-ticks", 20);

        if (cmode == null) return;

        switch (cmode) {
            case VConnection.STANDALONE:
                updateTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (wsServer != null) {
                            StatusData.sendUpdate(wsServer, useGzip);
                        }
                    }
                };
                break;
            case VConnection.PROXY:
                updateTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (bridgeClient != null) {
                            bridgeClient.sendStatusData();
                        }
                    }
                };
                break;
        }

        if (updateTask != null) {
            updateTask.runTaskTimer(this, 0L, intervalTicks);
        }
    }

    public void reloadPlugin() {
        reloadConfig();
        setupConfig();

        StatusData.init(this);

        cleanupNetwork();

        setupNetworkMode();

        startUpdateTask();

        getLogger().info("Configuration and tasks have been re-synchronized.");
    }

    private void cleanupNetwork() {
        if (wsServer != null) {
            try {
                wsServer.stop(1000);
            } catch (InterruptedException e) {
                getLogger().severe("WebSocket server stop error: " + e.getMessage());
            }
            wsServer = null;
        }

        if (bridgeClient != null) {
            bridgeClient.close();
            bridgeClient = null;
        }
    }

    @Override
    public void onDisable() {
        cleanupNetwork();
    }

    public OmniSightServer getWsServer() {
        return wsServer;
    }

    public void setupConfig() {
        saveDefaultConfig();

        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    public void setupNetworkMode() {
        String mode = getConfig().getString("velocity-connection", "AUTO").toUpperCase();

        if (mode.equals("PROXY")) {
            cmode = VConnection.PROXY;
            initProxySender();
            connectProxy();
        } else if (mode.equals("STANDALONE")) {
            cmode = VConnection.STANDALONE;
            startWebSocketServer();
        } else {
            getLogger().info("Velocity connection is set to AUTO. Attempting to connect to proxy...");

            initProxySender();

            if (connectProxy()) {
                cmode = VConnection.PROXY;
                getLogger().info("Successfully connected to Velocity! Operating in PROXY mode.");
            } else {
                cmode = VConnection.STANDALONE;
                if (bridgeClient != null) {
                    bridgeClient.close();
                    bridgeClient = null;
                }

                getLogger().info("Failed to connect to Velocity. Falling back to STANDALONE mode.");
                startWebSocketServer();
            }
        }
    }
}