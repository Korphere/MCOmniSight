package com.korphere.mcomnisight.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.korphere.mcomnisight.MCOmniSightVelocity;
import com.korphere.mcomnisight.provider.VStandardDataProvider;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executors;

public class InternalBridgeServer {

    private final MCOmniSightVelocity plugin;
    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    public InternalBridgeServer(MCOmniSightVelocity plugin, int port) {
        this.plugin = plugin;
        this.port = port;
    }

    public void start() {
        running = true;
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                serverSocket = new ServerSocket(port);
                plugin.getLogger().info("[Bridge] The TCP server for waiting for communications from Paper has been started on port {}", port);

                while (running && !serverSocket.isClosed()) {
                    Socket socket = serverSocket.accept();
                    Executors.newVirtualThreadPerTaskExecutor().submit(() -> handlePaperClient(socket));
                }
            } catch (Exception e) {
                if (running) {
                    plugin.getLogger().error("[Bridge] TCP Server Error: {}", e.getMessage());
                }
            }
        });
    }

    private void handlePaperClient(Socket socket) {
        try (socket;
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while (running && (line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                JsonObject paperData;
                try {
                    paperData = JsonParser.parseString(line).getAsJsonObject();
                } catch (Exception e) {
                    continue;
                }

                JsonObject combinedJson = combineWithVelocityData(paperData);

                String output = combinedJson.toString();

                if (output != null && !output.isBlank()) {
                    if (plugin.getWsServer() != null) {
                        plugin.getWsServer().broadcast(output);
                    }

                    if (plugin.getTcpServer() != null) {
                        plugin.getTcpServer().broadcast(output);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private JsonObject combineWithVelocityData(JsonObject paperData) {
        JsonObject combined = new JsonObject();

        for (Map.Entry<String, JsonElement> entry : paperData.entrySet()) {
            combined.add(entry.getKey(), entry.getValue());
        }

        JsonObject vStandard = VStandardDataProvider.getStandardData();
        if (vStandard != null && !vStandard.isEmpty()) {
            combined.add("standard_v", vStandard);
        }

        return combined;
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception e) {
            plugin.getLogger().error("[Bridge] TCP Server Stop Error: {}", e.getMessage());
        }
    }
}