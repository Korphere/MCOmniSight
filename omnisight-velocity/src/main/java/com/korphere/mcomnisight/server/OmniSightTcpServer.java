package com.korphere.mcomnisight.server;

import com.korphere.mcomnisight.ConfigManager;
import com.korphere.mcomnisight.MCOmniSightVelocity;
import com.korphere.mcomnisight.StatusData;
import com.korphere.mcomnisight.network.NetworkServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OmniSightTcpServer implements NetworkServer {
    private final MCOmniSightVelocity plugin;
    private final int port;
    private ServerSocket serverSocket;
    private boolean running = false;
    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    private String apiKey;

    public OmniSightTcpServer(int port, MCOmniSightVelocity plugin) {
        this.port = port;
        this.plugin = plugin;
        updateSettings();
    }

    @Override
    public void start() {
        running = true;
        threadPool.submit(() -> {
            try {
                serverSocket = new ServerSocket(port);
                plugin.getLogger().info("TCP Server started on port: {}", port);

                while (running && !serverSocket.isClosed()) {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(clientSocket);
                    clients.add(handler);
                    threadPool.submit(handler);
                }
            } catch (IOException e) {
                if (running) {
                    plugin.getLogger().error("TCP Server Error: {}", e.getMessage());
                }
            }
        });
    }

    @Override
    public void stop(int timeoutMs) throws InterruptedException {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            for (ClientHandler client : clients) {
                client.close();
            }
            clients.clear();
            threadPool.shutdown();
        } catch (IOException e) {
            plugin.getLogger().error("Error closing TCP server: {}", e.getMessage());
        }
    }

    @Override
    public void broadcast(String text) {
        byte[] bytes = (text + "\n").getBytes(StandardCharsets.UTF_8);
        broadcast(bytes);
    }

    @Override
    public void broadcast(byte[] bytes) {
        for (ClientHandler client : clients) {
            if (client.isAuthorized()) {
                client.sendBytes(bytes);
            }
        }
    }

    @Override
    public int getConnectedClientsCount() {
        return (int) clients.stream().filter(ClientHandler::isAuthorized).count();
    }

    public int getPort() {
        return port;
    }

    @Override
    public void updateSettings() {
        ConfigManager config = plugin.getConfigManager();
        this.apiKey = config.getApiKey();
    }

    public class ClientHandler implements Runnable {
        private final Socket socket;
        private OutputStream out;
        private BufferedReader in;
        private boolean authorized = false;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public boolean isAuthorized() {
            return authorized;
        }

        @Override
        @SuppressWarnings("StatementWithEmptyBody")
        public void run() {
            try {
                out = socket.getOutputStream();
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

                String authLine = in.readLine();
                if (authLine != null && authLine.equals("AUTH " + apiKey)) {
                    authorized = true;
                    plugin.getLogger().info("[TCP] Authorized client connected: {}", socket.getRemoteSocketAddress());

                    boolean useGzip = plugin.getConfigManager().isGzipEnabled();
                    StatusData.sendInitialFullDataTcp(this, useGzip);

                    String line;
                    while ((line = in.readLine()) != null) {
                    }
                } else {
                    plugin.getLogger().warn("[TCP] Auth failed for: {}", socket.getRemoteSocketAddress());
                    close();
                }
            } catch (Exception e) {
                plugin.getLogger().error("[TCP] Error: {}", e.getMessage());
            } finally {
                close();
                clients.remove(this);
            }
        }

        public void sendBytes(byte[] bytes) {
            try {
                if (out != null) {
                    out.write(bytes);
                    out.flush();
                }
            } catch (IOException e) {
                close();
            }
        }

        public void close() {
            try {
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException ignored) {}
        }
    }
}