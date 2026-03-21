package com.korphere.mcomnisight;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.List;

public class OmniSightServer extends WebSocketServer {
    private final MCOmniSight plugin;
    private String apiKey;
    private List<String> allowedIps;
    private boolean whitelistEnabled;

    public OmniSightServer(int port, MCOmniSight plugin) {
        super(new InetSocketAddress(port));
        this.plugin = plugin;
        updateSettings();
    }

    @Override
    public void onOpen(WebSocket conn, @NotNull ClientHandshake handshake) {
        String descriptor = handshake.getResourceDescriptor();

        plugin.getLogger().info("--- WebSocket Debug ---");
        plugin.getLogger().info("Received Descriptor: " + descriptor);
        plugin.getLogger().info("Config API Key: " + apiKey);

        if (apiKey.isEmpty() || !descriptor.contains("key=" + apiKey)) {
            plugin.getLogger().warning("[MCOmniSight] Invalid API Key attempt from: " + conn.getRemoteSocketAddress());
            conn.close(1008, "Invalid API Key");
            return;
        }

        String mode = plugin.getConfig().getString("connection-mode", "DIRECT").toUpperCase();
        String xff = handshake.getFieldValue("X-Forwarded-For");
        String remoteIp = conn.getRemoteSocketAddress().getAddress().getHostAddress();

        String clientIp = ((mode.equals("PROXY") || mode.equals("TUNNEL")) && xff != null && !xff.isEmpty())
                ? xff.split(",")[0].trim() : remoteIp;

        if (whitelistEnabled && !allowedIps.contains(clientIp)) {
            plugin.getLogger().warning("[MCOmniSight] Connection blocked by whitelist: " + clientIp);
            conn.close(1008, "IP Not Allowed");
            return;
        }

        boolean useGzip = plugin.getConfig().getBoolean("gzip-enabled", true);
        StatusData.sendInitialFullData(conn, useGzip);

        plugin.getLogger().info("[MCOmniSight] [" + mode + "] Authorized client connected: " + clientIp);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {}

    @Override
    public void onMessage(WebSocket conn, String message) {}

    @Override
    public void onError(WebSocket conn, Exception ex) {}

    @Override
    public void onStart() {
        plugin.getSLF4JLogger().info("OmniSight WebSocket Server started!");
    }

    public int getConnectedClientsCount() {
        return getConnections().size();
    }

    public void updateSettings() {
        this.apiKey = plugin.getConfig().getString("api-key", "default-key");
        this.whitelistEnabled = plugin.getConfig().getBoolean("whitelist.enabled", false);
        this.allowedIps = plugin.getConfig().getStringList("whitelist.allowed-ips");

        plugin.getLogger().info("WebSocket settings updated (Auth/Whitelist).");
        for (WebSocket conn : getConnections()) {
            conn.close(1012, "Server config reloaded. Please reconnect.");
        }
    }
}