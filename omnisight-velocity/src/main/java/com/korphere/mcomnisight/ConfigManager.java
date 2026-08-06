package com.korphere.mcomnisight;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import io.leangen.geantyref.TypeToken;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class ConfigManager {

    private final Path dataDirectory;
    private final Logger logger;
    private CommentedConfigurationNode configNode;

    private int webSocketPort = 8887;
    private int vPort = 8888;
    private int tcpPort = 8889;
    private String vHost = "127.0.0.1";
    private boolean gzipEnabled = true;
    private int updateIntervalTicks = 20;

    private boolean ws = true;
    private boolean tcp = true;

    private String apiKey = "";
    private String connectionMode = "DIRECT";
    private boolean whitelistEnabled = false;
    private List<String> allowedIps = Collections.emptyList();

    @Inject
    public ConfigManager(@DataDirectory Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    public void loadConfig() {
        try {
            if (Files.notExists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            Path configFile = dataDirectory.resolve("config.yml");

            if (Files.notExists(configFile)) {
                try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                    if (in != null) {
                        Files.copy(in, configFile);
                    } else {
                        Files.createFile(configFile);
                    }
                }
            }

            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .path(configFile)
                    .build();

            configNode = loader.load();

            this.webSocketPort = configNode.node("websocket-port").getInt(8887);
            this.vPort = configNode.node("proxy", "port").getInt(8888);
            this.vHost = configNode.node("proxy", "host").getString("127.0.0.1");
            this.tcpPort = configNode.node("tcp-port").getInt(8889);
            this.gzipEnabled = configNode.node("gzip-enabled").getBoolean(true);
            this.updateIntervalTicks = configNode.node("update-interval-ticks").getInt(20);

            this.ws = configNode.node("send","ws").getBoolean(true);
            this.tcp = configNode.node("send","tcp").getBoolean(true);

            this.apiKey = configNode.node("api-key").getString("");
            this.connectionMode = configNode.node("connection-mode").getString("DIRECT");
            this.whitelistEnabled = configNode.node("whitelist", "enabled").getBoolean(false);

            try {
                this.allowedIps = configNode.node("whitelist", "allowed-ips").getList(new TypeToken<String>() {}, Collections.emptyList());
            } catch (Exception e) {
                this.allowedIps = Collections.emptyList();
            }

            logger.info("Configuration loaded successfully!");

        } catch (Exception e) {
            logger.error("Failed to load config.yml", e);
        }
    }

    public int getWebSocketPort() { return webSocketPort; }
    public int getVPort() { return vPort; }
    public int getTcpPort() { return tcpPort; }
    public String getVHost() { return vHost; }
    public boolean isGzipEnabled() { return gzipEnabled; }
    public int getUpdateIntervalTicks() { return updateIntervalTicks; }
    public boolean getWsEnable() { return ws; }
    public boolean getTcpEnable() { return tcp; }
    public String getApiKey() { return apiKey; }
    public String getConnectionMode() { return connectionMode; }
    public boolean isWhitelistEnabled() { return whitelistEnabled; }
    public List<String> getAllowedIps() { return allowedIps; }

    public boolean getBoolean(String path, boolean defaultValue) {
        if (configNode == null) return defaultValue;

        String[] pathArray = path.split("\\.");

        ConfigurationNode node = configNode.node((Object[]) pathArray);

        if (!node.virtual() && node.raw() instanceof Boolean) {
            return node.getBoolean(defaultValue);
        }

        String[] enabledPathArray = (path + ".enabled").split("\\.");
        ConfigurationNode enabledNode = configNode.node((Object[]) enabledPathArray);

        if (!enabledNode.virtual() && enabledNode.raw() instanceof Boolean) {
            return enabledNode.getBoolean(defaultValue);
        }

        return defaultValue;
    }
}