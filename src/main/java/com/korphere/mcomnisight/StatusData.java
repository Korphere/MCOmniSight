package com.korphere.mcomnisight;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import com.google.gson.stream.JsonWriter;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;
import java.io.ByteArrayOutputStream;

import java.util.*;

public class StatusData {
    static boolean oshiAvailable = false;

    public static MCOmniSight plugin;
    private static volatile JsonObject lastFullData = null;

    public static void init(MCOmniSight instance) {
        plugin = instance;
        try {
            Class.forName("oshi.SystemInfo");
            oshiAvailable = true;
            plugin.getLogger().info("OSHI detected. Hardware monitoring enabled.");
        } catch (ClassNotFoundException e) {
            oshiAvailable = false;
            plugin.getLogger().info("OSHI not detected. Lite mode active.");
        }
    }

    public static @NotNull JsonObject collect() {
        FileConfiguration config = plugin.getConfig();
        JsonObject json = new JsonObject();
        Runtime runtime = Runtime.getRuntime();

        // Standard Status
        JsonObject standardData = StandardDataProvider.getStandardData();
        if (standardData != null) json.add("standard", standardData);
        // Standard Status

        // Performance
        if (config.getBoolean("features.performance.enabled", true)) {
            JsonObject performance = new JsonObject();

            if (config.getBoolean("features.performance.tps", true))
                performance.addProperty("tps", Math.min(20.0, Bukkit.getTPS()[0]));
            if (config.getBoolean("features.performance.mspt", true))
                performance.addProperty("mspt", Bukkit.getServer().getAverageTickTime());

            if (config.getBoolean("features.performance.memory.enabled", true)) {
                JsonObject memory = MemoryFromRuntimeProvider.getMemoryFromRuntime();
                if (memory != null) performance.add("memory", memory);
            }

            if (config.getBoolean("features.performance.cpu.enabled", true)) {
                JsonObject cpu = new JsonObject();

                if (config.getBoolean("features.performance.cpu.usable_cores", true))
                    cpu.addProperty("usable_cores", runtime.availableProcessors());

                performance.add("cpu", cpu);
            }

            json.add("performance", performance);
        }
        //Performance

        //Players
        JsonObject playersData = PlayerDataProvider.getPlayersData();
        if (playersData != null) json.add("players", playersData);
        //Players

        //World
        JsonObject worldData = WorldDataProvider.getWorldData();
        if (worldData != null) json.add("world_data", worldData);
        //World

        //Host Status
        if (oshiAvailable) {
            if (config.getBoolean("features.host_status.enabled", true)) {
                json.add("host_status", OshiProvider.getHostData());
            }
        }
        //Host Status
        return json;
    }

    public static byte @NotNull [] serializeWithGzip(JsonObject root, boolean useGzip) throws Exception {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        java.io.OutputStream targetStream = useGzip ? new GZIPOutputStream(byteStream) : byteStream;

        try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(targetStream, StandardCharsets.UTF_8))) {
            new Gson().toJson(root, writer);
        }

        return byteStream.toByteArray();
    }

    public static JsonObject getDelta(JsonObject current, JsonObject last) {
        if (last == null) return current;

        JsonObject delta = new JsonObject();
        for (String key : current.keySet()) {
            com.google.gson.JsonElement currentVal = current.get(key);
            com.google.gson.JsonElement lastVal = last.get(key);

            if (!currentVal.equals(lastVal)) {
                if (currentVal.isJsonObject() && lastVal != null && lastVal.isJsonObject()) {
                    JsonObject subDelta = getDelta(currentVal.getAsJsonObject(), lastVal.getAsJsonObject());
                    if (!subDelta.isEmpty()) {
                        delta.add(key, subDelta);
                    }
                } else {
                    delta.add(key, currentVal);
                }
            }
        }
        return delta;
    }

    public static void sendUpdate(OmniSightServer server, boolean useGzip) {
        JsonObject currentFullData = collect();

        final JsonObject previousData = lastFullData;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                JsonObject deltaData = getDelta(currentFullData, previousData);

                deltaData.addProperty("packet_type", "DELTA");
                deltaData.addProperty("_timestamp", System.currentTimeMillis());

                if (deltaData.isEmpty() || server.getConnections().isEmpty()) {
                    lastFullData = currentFullData;
                    return;
                }

                if (useGzip) {
                    byte[] payload = serializeWithGzip(deltaData, true);
                    server.broadcast(payload);
                } else {
                    String jsonString = deltaData.toString();
                    server.broadcast(jsonString);
                }

                lastFullData = currentFullData;

            } catch (Exception e) {
                plugin.getLogger().severe("配信エラー: " + e.getMessage());
            }
        });
    }

    public static void sendInitialFullData(org.java_websocket.WebSocket conn, boolean useGzip) {
        JsonObject currentData = collect();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                currentData.addProperty("packet_type", "FULL_DATA");

                if (lastFullData == null) {
                    lastFullData = currentData;
                }

                if (useGzip) {
                    byte[] payload = serializeWithGzip(currentData, true);
                    conn.send(payload);
                } else {
                    String jsonString = currentData.toString();
                    conn.send(jsonString);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("初期データ送信エラー: " + e.getMessage());
            }
        });
    }
}
