package com.korphere.mcomnisight;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.korphere.mcomnisight.StatusData.plugin;

public class PlayerDataProvider {
    private static final FileConfiguration config = plugin.getConfig();
    private static final PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();
    private static final GsonComponentSerializer gsonSerializer = GsonComponentSerializer.gson();
    private static final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacySection();
    public static @Nullable JsonObject getPlayersData() {
        if (!plugin.getConfig().getBoolean("features.players.enabled", true) || !plugin.getConfig().getBoolean("features.players.players_list.enabled", true)) {
            return null;
        }
        JsonObject players = new JsonObject();
        JsonArray playersList = new JsonArray();
        for (Player player : Bukkit.getOnlinePlayers()) {
            playersList.add(getSinglePlayerData(player));
        }
        players.add("players_list", playersList);
        return players;
    }

    private static @NotNull JsonObject getSinglePlayerData(@NotNull Player player) {

        String path = "features.players.players_list.player_data.";
        Map<String, Supplier<Object>> entryMap = new LinkedHashMap<>();

        entryMap.put("name", player::getName);
        entryMap.put("display_name_plain", () -> plainSerializer.serialize(player.name()));
        entryMap.put("display_name_json", () -> gsonSerializer.serialize(player.name()));
        entryMap.put("display_name_legacy", () -> legacySerializer.serialize(player.name()));
        if (config.getBoolean(path + "player_list_name", true)) {
            Component listName = player.playerListName();
            entryMap.put("player_list_name_plain", () -> plainSerializer.serialize(listName));
            entryMap.put("player_list_name_json", () -> gsonSerializer.serialize(listName));
            entryMap.put("player_list_name_legacy", () -> legacySerializer.serialize(listName));
        }
        entryMap.put("uuid", () -> player.getUniqueId().toString());
        entryMap.put("world", () -> player.getWorld().getName());
        entryMap.put("hp", player::getHealth);
        entryMap.put("hp_scale", player::getHealthScale);
        entryMap.put("max_hp", () -> getAttr(player, Attribute.GENERIC_MAX_HEALTH, 20.0));
        entryMap.put("armor", () -> getAttr(player, Attribute.GENERIC_ARMOR, 0.0));
        entryMap.put("armor_toughness", () -> getAttr(player, Attribute.GENERIC_ARMOR_TOUGHNESS, 0.0));
        entryMap.put("attack_damage", () -> getAttr(player, Attribute.GENERIC_ATTACK_DAMAGE, 2.0));
        entryMap.put("attack_knockback", () -> getAttr(player, Attribute.GENERIC_ATTACK_KNOCKBACK, 0.0));
        entryMap.put("attack_speed", () -> getAttr(player, Attribute.GENERIC_ATTACK_SPEED, 4.0));
        entryMap.put("burning_time", () -> getAttr(player, Attribute.GENERIC_BURNING_TIME, 1.0));
        entryMap.put("explosion_knockback_resistance", () -> getAttr(player, Attribute.GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE, 0.0));
        entryMap.put("fall_damage_multiplier", () -> getAttr(player, Attribute.GENERIC_FALL_DAMAGE_MULTIPLIER, 1.0));
        entryMap.put("flying_speed", () -> getAttr(player, Attribute.GENERIC_FLYING_SPEED, 0.02));
        entryMap.put("follow_range", () -> getAttr(player, Attribute.GENERIC_FOLLOW_RANGE, 32.0));
        entryMap.put("gravity", () -> getAttr(player, Attribute.GENERIC_GRAVITY, 0.08));
        entryMap.put("jump_strength", () -> getAttr(player, Attribute.GENERIC_JUMP_STRENGTH, 0.42));
        entryMap.put("knockback_resistance", () -> getAttr(player, Attribute.GENERIC_KNOCKBACK_RESISTANCE, 0.0));
        entryMap.put("luck", () -> getAttr(player, Attribute.GENERIC_LUCK, 0.0));
        entryMap.put("max_absorption", () -> getAttr(player, Attribute.GENERIC_MAX_ABSORPTION, 0.0));
        entryMap.put("movement_efficiency", () -> getAttr(player, Attribute.GENERIC_MOVEMENT_EFFICIENCY, 0.0));
        entryMap.put("movement_speed", () -> getAttr(player, Attribute.GENERIC_MOVEMENT_SPEED, 0.1));
        entryMap.put("oxygen_bonus", () -> getAttr(player, Attribute.GENERIC_OXYGEN_BONUS, 0.0));
        entryMap.put("safe_fall_distance", () -> getAttr(player, Attribute.GENERIC_SAFE_FALL_DISTANCE, 3.0));
        entryMap.put("scale", () -> getAttr(player, Attribute.GENERIC_SCALE, 1.0));
        entryMap.put("step_height", () -> getAttr(player, Attribute.GENERIC_STEP_HEIGHT, 0.6));
        entryMap.put("water_movement_efficiency", () -> getAttr(player, Attribute.GENERIC_WATER_MOVEMENT_EFFICIENCY, 0.0));
        entryMap.put("player_block_break_speed", () -> getAttr(player, Attribute.PLAYER_BLOCK_BREAK_SPEED, 1.0));
        entryMap.put("player_block_interaction_range", () -> getAttr(player, Attribute.PLAYER_BLOCK_INTERACTION_RANGE, 4.5));
        entryMap.put("player_entity_interaction_range", () -> getAttr(player, Attribute.PLAYER_ENTITY_INTERACTION_RANGE, 3.0));
        entryMap.put("player_mining_efficiency", () -> getAttr(player, Attribute.PLAYER_MINING_EFFICIENCY, 0.0));
        entryMap.put("player_sneaking_speed", () -> getAttr(player, Attribute.PLAYER_SNEAKING_SPEED, 0.3));
        entryMap.put("player_submerged_mining_speed", () -> getAttr(player, Attribute.PLAYER_SUBMERGED_MINING_SPEED, 0.2));
        entryMap.put("player_sweeping_damage_ratio", () -> getAttr(player, Attribute.PLAYER_SWEEPING_DAMAGE_RATIO, 0.0));
        entryMap.put("hunger", player::getFoodLevel);
        entryMap.put("game_mode", () -> player.getGameMode().name());
        entryMap.put("prev_game_mode", () -> player.getPreviousGameMode() != null ? player.getPreviousGameMode().name() : "NONE");
        entryMap.put("level", player::getLevel);
        entryMap.put("exp", player::getExp);
        entryMap.put("exp_cooldown", player::getExpCooldown);
        entryMap.put("total_exp", player::getTotalExperience);
        entryMap.put("locale", () -> player.locale().toString()); // Deprecated
        entryMap.put("ping", player::getPing);
        entryMap.put("host", () -> player.getAddress() != null ? player.getAddress().getHostName() : "NONE");
        entryMap.put("host_string", () -> player.getAddress() != null ? player.getAddress().getHostString() : "NONE");
        entryMap.put("address", () -> player.getAddress() != null ? String.valueOf(player.getAddress().getAddress()) : "NONE");
        entryMap.put("port", () -> player.getAddress() != null ? player.getAddress().getPort() : 0);
        if (config.getBoolean(path + "footer", true)) {
            Component footer = player.playerListFooter();
            if (footer != null) {
                entryMap.put("footer_plain", () -> plainSerializer.serialize(footer));
                entryMap.put("footer_json", () -> gsonSerializer.serialize(footer));
                entryMap.put("footer_legacy", () -> legacySerializer.serialize(footer));
            }
        }
        if (config.getBoolean(path + "header", true)) {
            Component header = player.playerListHeader();
            if (header != null) {
                entryMap.put("header_plain", () -> plainSerializer.serialize(header));
                entryMap.put("header_json", () -> gsonSerializer.serialize(header));
                entryMap.put("header_legacy", () -> legacySerializer.serialize(header));
            }
        }
        entryMap.put("time", player::getPlayerTime);
        entryMap.put("time_offset", player::getPlayerTimeOffset);
        entryMap.put("weather", () -> player.getPlayerWeather() != null ? player.getPlayerWeather().name() : "NONE");
        entryMap.put("client_view_distance", player::getClientViewDistance);
        entryMap.put("compass_target", () -> String.valueOf(player.getCompassTarget()));
        entryMap.put("fly_speed", player::getFlySpeed);
        entryMap.put("walk_speed", player::getWalkSpeed);
        entryMap.put("respawn", () -> player.getRespawnLocation() != null ? player.getRespawnLocation().toString() : "NONE");

        entryMap.put("location", () -> PlayerLocationsProvider.getPlayerLocations(player));
        entryMap.put("permissions", () -> PlayerPermissionsProvider.getPlayerPermissions(player));
        entryMap.put("states", () -> PlayerStatesProvider.getPlayerStates(player));
        entryMap.put("statistics", () -> PlayerStatisticsProvider.getPlayerStatistics(player));

        JsonObject data = Utils.serializeFromMap(entryMap, path);

        if (config.getBoolean(path + "inventory.enabled", true)) {
            JsonObject inventory = PlayerInventoryProvider.getInventoryData(player);
            if (inventory != null && !inventory.entrySet().isEmpty()) {
                data.add("inventory", inventory);
            }
        }

        return data;
    }

    private static double getAttr(@NotNull Player p, Attribute a, double def) {
        AttributeInstance ai = p.getAttribute(a);
        return ai != null ? ai.getValue() : def;
    }
}
