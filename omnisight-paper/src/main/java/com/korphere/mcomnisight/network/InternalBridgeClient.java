package com.korphere.mcomnisight.network;

import com.google.gson.JsonObject;
import com.korphere.mcomnisight.MCOmniSightPaper;
import com.korphere.mcomnisight.StatusData;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class InternalBridgeClient {

    private final MCOmniSightPaper plugin;
    private Socket socket;
    private PrintWriter writer;

    public InternalBridgeClient(MCOmniSightPaper plugin) {
        this.plugin = plugin;
    }

    /**
     * Attempting to connect to Velocity.
     * @param host Host
     * @param port Port
     * @return if success to connect, true
     */
    public boolean connect(String host, int port) {
        try {
            this.socket = new Socket();
            this.socket.connect(new InetSocketAddress(host, port), 1500);

            this.writer = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),
                    true
            );

            plugin.getLogger().info("I connected to Velocity's internal TCP server.");
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to connect to Velocity: " + e.getMessage());
            close();
            return false;
        }
    }

    public void sendJsonData(String json) {
        if (writer != null && socket != null && !socket.isClosed()) {
            writer.println(json);
        }
    }

    public void sendJsonData(JsonObject json) {
        if (json != null) {
            sendJsonData(json.toString());
        }
    }

    public void sendStatusData() {
        JsonObject data = StatusData.collect();
        sendJsonData(data);
    }

    public void close() {
        try {
            if (writer != null) {
                writer.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception ignored) {}
    }
}