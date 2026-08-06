package com.korphere.mcomnisight;

import com.google.inject.Inject;
import com.korphere.mcomnisight.listener.VOmniSightEventListener;
import com.korphere.mcomnisight.provider.VMetricsRunner;
import com.korphere.mcomnisight.server.InternalBridgeServer;
import com.korphere.mcomnisight.server.OmniSightServer;
import com.korphere.mcomnisight.server.OmniSightTcpServer;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import org.slf4j.Logger;

import java.time.Duration;

import static com.korphere.mcomnisight.StatusData.oshiAvailable;
import static com.korphere.mcomnisight.StatusData.plugin;

@Plugin(
        id = "mcomnisight",
        name = "MCOmniSight",
        version = "2.1.3",
        description = "Let's watch Real-time server status.",
        authors = {"KoHaRxnP", "Korphere"}
)
public final class MCOmniSightVelocity {

    private final ProxyServer server;
    private final Logger logger;
    private final ConfigManager configManager;

    private OmniSightServer wsServer;
    private OmniSightTcpServer tcpServer;
    private InternalBridgeServer bridgeServer;
    private ScheduledTask updateTask;
    private ScheduledTask oshiTask;

    @Inject
    public MCOmniSightVelocity(ProxyServer server, Logger logger, ConfigManager configManager) {
        this.server = server;
        this.logger = logger;
        this.configManager = configManager;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        setupConfig();

        CommandManager commandManager = server.getCommandManager();
        CommandMeta meta = commandManager.metaBuilder("omnisight")
                .plugin(this)
                .build();

        commandManager.register(meta, new OmniSightCommand(this));

        StatusData.init(this);

        startWebSocketServer();
        startTcpServer();
        startBridgeServer();

        server.getEventManager().register(this, new VOmniSightEventListener(this));

        if (oshiAvailable) {
            try {
                VMetricsRunner runner = (VMetricsRunner) Class.forName("com.korphere.mcomnisight.provider.VOshiRunner")
                        .getDeclaredConstructor()
                        .newInstance();

                oshiTask = server.getScheduler()
                        .buildTask(this, runner::update)
                        .repeat(Duration.ofSeconds(1))
                        .schedule();

            } catch (Exception e) {
                logger.warn("OSHI Init Failed: {}", e.getMessage());
            }
        }

        startUpdateTask();
    }

    private void startWebSocketServer() {
        int port = configManager.getWebSocketPort();
        wsServer = new OmniSightServer(port, this);
        wsServer.start();
        logger.info("WebSocket Server started on port: {}", port);
    }

    private void startTcpServer() {
        int tcpPort = configManager.getTcpPort();
        tcpServer = new OmniSightTcpServer(tcpPort, this);
        tcpServer.start();
        logger.info("TCP Server started on port: {}", tcpPort);
    }

    private void startBridgeServer() {
        int bridgePort = configManager.getVPort();
        bridgeServer = new InternalBridgeServer(this, bridgePort);
        bridgeServer.start();
    }

    private void startUpdateTask() {
        if (updateTask != null) updateTask.cancel();

        boolean useGzip = configManager.isGzipEnabled();
        int intervalTicks = configManager.getUpdateIntervalTicks();

        long intervalMillis = intervalTicks * 50L;

        updateTask = server.getScheduler()
                .buildTask(this, () -> {
                    if (wsServer != null && plugin.getConfigManager().getBoolean("send.ws", false)) {
                        StatusData.sendUpdate(wsServer, useGzip);
                    }
                    if (tcpServer != null && plugin.getConfigManager().getBoolean("send.tcp", false)) {
                        StatusData.sendUpdate(tcpServer, useGzip);
                    }
                })
                .repeat(Duration.ofMillis(intervalMillis))
                .schedule();
    }

    public void reloadPlugin() {
        setupConfig();
        StatusData.init(this);

        int newWsPort = configManager.getWebSocketPort();
        if (wsServer == null || wsServer.getPort() != newWsPort) {
            logger.info("Port change detected. Restarting WebSocket server...");
            try {
                if (wsServer != null) wsServer.stop(1000);
                startWebSocketServer();
            } catch (InterruptedException e) {
                logger.error("Failed to restart the WebSocket server.", e);
            }
        } else {
            wsServer.updateSettings();
        }
        int newTcpPort = configManager.getTcpPort();
        if (tcpServer == null || tcpServer.getPort() != newTcpPort) {
            logger.info("Port change detected. Restarting TCP server...");
            try {
                if (tcpServer != null) tcpServer.stop(1000);
                startTcpServer();
            } catch (InterruptedException e) {
                logger.error("Failed to restart the TCP server.", e);
            }
        } else {
            tcpServer.updateSettings();
        }

        if (bridgeServer != null) {
            bridgeServer.stop();
        }
        startBridgeServer();

        startUpdateTask();
        logger.info("Configuration and tasks have been re-synchronized.");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (updateTask != null) updateTask.cancel();
        if (oshiTask != null) oshiTask.cancel();

        if (bridgeServer != null) {
            bridgeServer.stop();
        }

        if (wsServer != null) {
            try {
                wsServer.stop(1000);
            } catch (InterruptedException e) {
                logger.error("WebSocket server stop interrupted", e);
            }
        }

        if (tcpServer != null) {
            try {
                tcpServer.stop(1000);
            } catch (InterruptedException e) {
                logger.error("TCP server stop interrupted", e);
            }
        }
    }

    public OmniSightServer getWsServer() {
        return wsServer;
    }

    public OmniSightTcpServer getTcpServer() {
        return tcpServer;
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void setupConfig() {
        configManager.loadConfig();
    }
}