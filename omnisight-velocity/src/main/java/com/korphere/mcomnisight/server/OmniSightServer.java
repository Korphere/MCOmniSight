package com.korphere.mcomnisight.server;

import com.korphere.mcomnisight.ConfigManager;
import com.korphere.mcomnisight.MCOmniSightVelocity;
import com.korphere.mcomnisight.StatusData;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.List;

public class OmniSightServer extends WebSocketServer {
    private final MCOmniSightVelocity plugin;
    private String apiKey;
    private List<String> allowedIps;
    private boolean whitelistEnabled;

    public OmniSightServer(int port, MCOmniSightVelocity plugin) {
        super(new InetSocketAddress(port));
        this.plugin = plugin;
        updateSettings();
    }

    @Override
    public void onOpen(WebSocket conn, @NotNull ClientHandshake handshake) {
        String descriptor = handshake.getResourceDescriptor();

        plugin.getLogger().info("--- WebSocket Debug ---");
        plugin.getLogger().info("Received Descriptor: {}", descriptor);
        plugin.getLogger().info("Config API Key: {}", apiKey);

        if (apiKey.isEmpty() || !descriptor.contains("key=" + apiKey)) {
            plugin.getLogger().warn("[MCOmniSight] Invalid API Key attempt from: {}", conn.getRemoteSocketAddress());
            conn.close(1008, "Invalid API Key");
            return;
        }

        ConfigManager config = plugin.getConfigManager();
        String mode = config.getConnectionMode().toUpperCase();
        String xff = handshake.getFieldValue("X-Forwarded-For");
        String remoteIp = conn.getRemoteSocketAddress().getAddress().getHostAddress();

        String clientIp = ((mode.equals("PROXY") || mode.equals("TUNNEL")) && xff != null && !xff.isEmpty())
                ? xff.split(",")[0].trim() : remoteIp;

        if (whitelistEnabled && !allowedIps.contains(clientIp)) {
            plugin.getLogger().warn("[MCOmniSight] Connection blocked by whitelist: {}", clientIp);
            conn.close(1008, "IP Not Allowed");
            return;
        }

        boolean useGzip = config.isGzipEnabled();
        StatusData.sendInitialFullData(conn, useGzip);

        plugin.getLogger().info("[MCOmniSight] [{}] Authorized client connected: {}", mode, clientIp);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {}

    @Override
    public void onMessage(WebSocket conn, String message) {}

    @Override
    public void onError(WebSocket conn, Exception ex) {}

    @Override
    public void onStart() {
        plugin.getLogger().info("OmniSight WebSocket Server started!");
    }

    public int getConnectedClientsCount() {
        return getConnections().size();
    }

    public void updateSettings() {
        ConfigManager config = plugin.getConfigManager();
        this.apiKey = config.getApiKey();
        this.whitelistEnabled = config.isWhitelistEnabled();
        this.allowedIps = config.getAllowedIps();

        plugin.getLogger().info("WebSocket settings updated (Auth/Whitelist).");
        for (WebSocket conn : getConnections()) {
            conn.close(1012, "Server config reloaded. Please reconnect.");
        }
    }
}