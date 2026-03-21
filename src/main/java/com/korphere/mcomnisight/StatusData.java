package com.korphere.mcomnisight;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.google.gson.stream.JsonWriter;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;
import java.io.ByteArrayOutputStream;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.*;

public class StatusData {
    static boolean oshiAvailable = false;

    public static MCOmniSight plugin;
    private static JsonObject lastFullData = null;

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
        PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();
        GsonComponentSerializer gsonSerializer = GsonComponentSerializer.gson();
        LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacySection();

        // Standard Status
        if (config.getBoolean("features.standard.enabled", true)) {
            if (config.getBoolean("features.standard.online_players", true)) json.addProperty("online_players", Bukkit.getOnlinePlayers().size());
            if (config.getBoolean("features.standard.max_players", true)) json.addProperty("max_players", Bukkit.getMaxPlayers());
        }
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
        if (config.getBoolean("features.players.enabled", true)) {
            JsonObject players = new JsonObject();
            if (config.getBoolean("features.players.players_list.enabled", true)) {
                JsonArray playersList = new JsonArray();
                String player_data_path = "features.players.players_list.player_data.";
                for (Player player : Bukkit.getOnlinePlayers()) {
                    JsonObject playerData = new JsonObject();

                    if (plugin.getConfig().getBoolean(player_data_path + "name", true)) playerData.addProperty("name", player.getName());
                    if (config.getBoolean(player_data_path + "display_name", true)) {
                        Component name = player.name();
                        playerData.addProperty("display_name_plain", plainSerializer.serialize(name));
                        playerData.addProperty("display_name_json", gsonSerializer.serialize(name));
                        playerData.addProperty("display_name_legacy", legacySerializer.serialize(name));
                    }
                    if (config.getBoolean(player_data_path + "player_list_name", true)) {
                        Component listName = player.playerListName();
                        playerData.addProperty("player_list_name_plain", plainSerializer.serialize(listName));
                        playerData.addProperty("player_list_name_json", gsonSerializer.serialize(listName));
                        playerData.addProperty("player_list_name_legacy", legacySerializer.serialize(listName));
                    }
                    if (plugin.getConfig().getBoolean(player_data_path + "uuid", true)) playerData.addProperty("uuid", player.getUniqueId().toString());
                    if (plugin.getConfig().getBoolean(player_data_path + "world", true)) playerData.addProperty("world", player.getWorld().getName());
                    if (plugin.getConfig().getBoolean(player_data_path + "hp", true)) playerData.addProperty("hp", player.getHealth());
                    if (plugin.getConfig().getBoolean(player_data_path + "hp_scale", true)) playerData.addProperty("hp_scale", player.getHealthScale());
                    if (config.getBoolean(player_data_path + "max_hp", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                        playerData.addProperty("max_hp", attr != null ? attr.getValue() : 20.0);
                    }
                    if (config.getBoolean(player_data_path + "armor", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_ARMOR);
                        playerData.addProperty("armor", attr != null ? attr.getValue() : 0.0);
                    }
                    if (config.getBoolean(player_data_path + "armor_toughness", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS);
                        playerData.addProperty("armor_toughness", attr != null ? attr.getValue() : 0.0);
                    }
                    if (config.getBoolean(player_data_path + "attack_damage", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
                        playerData.addProperty("attack_damage", attr != null ? attr.getValue() : 2.0); // プレイヤーの素手攻撃力
                    }
                    if (config.getBoolean(player_data_path + "attack_knockback", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_ATTACK_KNOCKBACK);
                        playerData.addProperty("attack_knockback", attr != null ? attr.getValue() : 0.0);
                    }
                    if (config.getBoolean(player_data_path + "attack_speed", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
                        playerData.addProperty("attack_speed", attr != null ? attr.getValue() : 4.0);
                    }
                    if (config.getBoolean(player_data_path + "burning_time", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_BURNING_TIME);
                        playerData.addProperty("burning_time", attr != null ? attr.getValue() : 1.0);
                    }
                    if (config.getBoolean(player_data_path + "explosion_knockback_resistance", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE);
                        playerData.addProperty("explosion_knockback_resistance", attr != null ? attr.getValue() : 0.0);
                    }
                    if (config.getBoolean(player_data_path + "fall_damage_multiplier", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_FALL_DAMAGE_MULTIPLIER);
                        playerData.addProperty("fall_damage_multiplier", attr != null ? attr.getValue() : 1.0);
                    }
                    if (config.getBoolean(player_data_path + "flying_speed", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_FLYING_SPEED);
                        playerData.addProperty("flying_speed", attr != null ? attr.getValue() : 0.02);
                    }
                    if (config.getBoolean(player_data_path + "follow_range", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_FOLLOW_RANGE);
                        playerData.addProperty("follow_range", attr != null ? attr.getValue() : 32.0);
                    }
                    if (config.getBoolean(player_data_path + "gravity", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_GRAVITY);
                        playerData.addProperty("gravity", attr != null ? attr.getValue() : 0.08);
                    }
                    if (config.getBoolean(player_data_path + "jump_strength", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH);
                        playerData.addProperty("jump_strength", attr != null ? attr.getValue() : 0.42);
                    }
                    if (config.getBoolean(player_data_path + "knockback_resistance", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
                        playerData.addProperty("knockback_resistance", attr != null ? attr.getValue() : 0.0);
                    }
                    if (config.getBoolean(player_data_path + "luck", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_LUCK);
                        playerData.addProperty("luck", attr != null ? attr.getValue() : 0.0);
                    }
                    if (config.getBoolean(player_data_path + "max_absorption", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_ABSORPTION);
                        playerData.addProperty("max_absorption", attr != null ? attr.getValue() : 0.0);
                    }
                    if (config.getBoolean(player_data_path + "movement_efficiency", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MOVEMENT_EFFICIENCY);
                        playerData.addProperty("movement_efficiency", attr != null ? attr.getValue() : 0.0);
                    }
                    if (config.getBoolean(player_data_path + "movement_speed", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                        playerData.addProperty("movement_speed", attr != null ? attr.getValue() : 0.1);
                    }
                    if (config.getBoolean(player_data_path + "oxygen_bonus", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_OXYGEN_BONUS);
                        playerData.addProperty("oxygen_bonus", attr != null ? attr.getValue() : 0.0);
                    }
                    if (config.getBoolean(player_data_path + "safe_fall_distance", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_SAFE_FALL_DISTANCE);
                        playerData.addProperty("safe_fall_distance", attr != null ? attr.getValue() : 3.0);
                    }
                    if (config.getBoolean(player_data_path + "scale", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_SCALE);
                        playerData.addProperty("scale", attr != null ? attr.getValue() : 1.0);
                    }
                    if (config.getBoolean(player_data_path + "step_height", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_STEP_HEIGHT);
                        playerData.addProperty("step_height", attr != null ? attr.getValue() : 0.6);
                    }
                    if (config.getBoolean(player_data_path + "water_movement_efficiency", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_WATER_MOVEMENT_EFFICIENCY);
                        playerData.addProperty("water_movement_efficiency", attr != null ? attr.getValue() : 0.0);
                    }
                    if (config.getBoolean(player_data_path + "player_block_break_speed", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.PLAYER_BLOCK_BREAK_SPEED);
                        playerData.addProperty("player_block_break_speed", attr != null ? attr.getValue() : 1.0);
                    }
                    if (config.getBoolean(player_data_path + "player_block_interaction_range", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.PLAYER_BLOCK_INTERACTION_RANGE);
                        playerData.addProperty("player_block_interaction_range", attr != null ? attr.getValue() : 4.5);
                    }
                    if (config.getBoolean(player_data_path + "player_entity_interaction_range", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE);
                        playerData.addProperty("player_entity_interaction_range", attr != null ? attr.getValue() : 3.0);
                    }
                    if (config.getBoolean(player_data_path + "player_mining_efficiency", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.PLAYER_MINING_EFFICIENCY);
                        playerData.addProperty("player_mining_efficiency", attr != null ? attr.getValue() : 0.0);
                    }
                    if (config.getBoolean(player_data_path + "player_sneaking_speed", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.PLAYER_SNEAKING_SPEED);
                        playerData.addProperty("player_sneaking_speed", attr != null ? attr.getValue() : 0.3);
                    }
                    if (config.getBoolean(player_data_path + "player_submerged_mining_speed", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.PLAYER_SUBMERGED_MINING_SPEED);
                        playerData.addProperty("player_submerged_mining_speed", attr != null ? attr.getValue() : 0.2);
                    }
                    if (config.getBoolean(player_data_path + "player_sweeping_damage_ratio", true)) {
                        AttributeInstance attr = player.getAttribute(Attribute.PLAYER_SWEEPING_DAMAGE_RATIO);
                        playerData.addProperty("player_sweeping_damage_ratio", attr != null ? attr.getValue() : 0.0);
                    }
                    if (plugin.getConfig().getBoolean(player_data_path + "hunger", true)) playerData.addProperty("hunger", player.getFoodLevel());
                    if (plugin.getConfig().getBoolean(player_data_path + "game_mode", true)) playerData.addProperty("game_mode", player.getGameMode().name());
                    if (plugin.getConfig().getBoolean(player_data_path + "prev_game_mode", true)) playerData.addProperty("prev_game_mode", player.getPreviousGameMode() != null ? player.getPreviousGameMode().name() : "NONE");
                    if (plugin.getConfig().getBoolean(player_data_path + "level", true)) playerData.addProperty("level", player.getLevel());
                    if (plugin.getConfig().getBoolean(player_data_path + "exp", true)) playerData.addProperty("exp", player.getExp());
                    if (plugin.getConfig().getBoolean(player_data_path + "exp_cooldown", true)) playerData.addProperty("exp_cooldown", player.getExpCooldown());
                    if (plugin.getConfig().getBoolean(player_data_path + "total_exp", true)) playerData.addProperty("total_exp", player.getTotalExperience());
                    if (plugin.getConfig().getBoolean(player_data_path + "locale", true)) playerData.addProperty("locale", player.locale().toString()); // Deprecated
                    if (plugin.getConfig().getBoolean(player_data_path + "ping", true)) playerData.addProperty("ping", player.getPing());
                    if (plugin.getConfig().getBoolean(player_data_path + "host", true)) playerData.addProperty("host", player.getAddress() != null ? player.getAddress().getHostName() : "NONE");
                    if (plugin.getConfig().getBoolean(player_data_path + "host_string", true)) playerData.addProperty("host_string", player.getAddress() != null ? player.getAddress().getHostString() : "NONE");
                    if (plugin.getConfig().getBoolean(player_data_path + "address", true)) playerData.addProperty("address", player.getAddress() != null ? String.valueOf(player.getAddress().getAddress()) : "NONE");
                    if (plugin.getConfig().getBoolean(player_data_path + "port", true)) playerData.addProperty("port", player.getAddress() != null ? player.getAddress().getPort() : 0);
                    if (config.getBoolean(player_data_path + "footer", true)) {
                        Component footer = player.playerListFooter();
                        if (footer != null) {
                            playerData.addProperty("footer_plain", plainSerializer.serialize(footer));
                            playerData.addProperty("footer_json", gsonSerializer.serialize(footer));
                            playerData.addProperty("footer_legacy", legacySerializer.serialize(footer));
                        }
                    }
                    if (config.getBoolean(player_data_path + "header", true)) {
                        Component header = player.playerListHeader();
                        if (header != null) {
                            playerData.addProperty("header_plain", plainSerializer.serialize(header));
                            playerData.addProperty("header_json", gsonSerializer.serialize(header));
                            playerData.addProperty("header_legacy", legacySerializer.serialize(header));
                        }
                    }
                    if (plugin.getConfig().getBoolean(player_data_path + "time", true)) playerData.addProperty("time", player.getPlayerTime());
                    if (plugin.getConfig().getBoolean(player_data_path + "time_offset", true)) playerData.addProperty("time_offset", player.getPlayerTimeOffset());
                    if (plugin.getConfig().getBoolean(player_data_path + "weather", true)) playerData.addProperty("weather", player.getPlayerWeather() != null ? player.getPlayerWeather().name() : "NONE");
                    if (plugin.getConfig().getBoolean(player_data_path + "client_view_distance", true)) playerData.addProperty("clint_view_distance", player.getClientViewDistance());
                    if (plugin.getConfig().getBoolean(player_data_path + "compass_target", true)) playerData.addProperty("compass_target", String.valueOf(player.getCompassTarget()));
                    if (plugin.getConfig().getBoolean(player_data_path + "fly_speed", true)) playerData.addProperty("fly_speed", player.getFlySpeed());
                    if (plugin.getConfig().getBoolean(player_data_path + "walk_speed", true)) playerData.addProperty("walk_speed", player.getWalkSpeed());
                    if (config.getBoolean(player_data_path + "respawn", true)) {
                        Location respawn = player.getRespawnLocation();
                        playerData.addProperty("respawn", respawn != null ? respawn.toString() : "NONE");
                    }

                    JsonObject locations = PlayerLocationsProvider.getPlayerLocations(player);
                    if (locations != null) playerData.add("location", locations);

                    JsonObject permissions = PlayerPermissionsProvider.getPlayerPermissions(player);
                    if(permissions != null) playerData.add("permissions", permissions);

                    JsonObject states = PlayerStatesProvider.getPlayerStates(player);
                    if (states != null) playerData.add("states", states);

                    JsonObject statistics = PlayerStatisticsProvider.getPlayerStatistics(player);
                    if (statistics != null) playerData.add("statistics", statistics);

                    JsonObject inventory = PlayerInventoryProvider.getInventoryData(player);
                    if (inventory != null && !inventory.entrySet().isEmpty()) playerData.add("inventory", inventory);

                    playersList.add(playerData);
                }
                players.add("players_list", playersList);
            }

            json.add("players", players);
        }
        //Players

        //World
        JsonObject worldData = WorldDataProvider.getWorldData();
        if (worldData != null) worldData.add("worldData", worldData);
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (lastFullData == null) {
                    lastFullData = collect();
                }

                if (useGzip) {
                    byte[] payload = serializeWithGzip(lastFullData, true);
                    conn.send(payload);
                } else {
                    String jsonString = lastFullData.toString();
                    conn.send(jsonString);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("初期データ送信エラー: " + e.getMessage());
            }
        });
    }
}
