package com.korphere.mcomnisight;

import com.google.gson.JsonObject;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.korphere.mcomnisight.StatusData.plugin;

public class PlayerLocationsProvider {
    public static @Nullable JsonObject getPlayerLocations(@NotNull Player player) {
        if (!plugin.getConfig().getBoolean("features.players.players_list.player_data.location.enabled", true)) {
            return null;
        }
        String path = "features.players.players_list.player_data.location.";

        Location loc = player.getLocation();

        Map<String, Supplier<Object>> entryMap = new LinkedHashMap<>();

        entryMap.put("x", () -> Math.round(loc.getX()));
        entryMap.put("y", () -> Math.round(loc.getY()));
        entryMap.put("z", () -> Math.round(loc.getZ()));

        entryMap.put("accurate_x", loc::getX);
        entryMap.put("accurate_y", loc::getY);
        entryMap.put("accurate_z", loc::getZ);

        entryMap.put("yaw", () -> Math.round(loc.getYaw()));
        entryMap.put("pitch", () -> Math.round(loc.getPitch()));
        entryMap.put("accurate_yaw", loc::getYaw);
        entryMap.put("accurate_pitch", loc::getPitch);

        entryMap.put("block", () -> loc.getBlock().getType().name());
        entryMap.put("block_x", loc::getBlockX);
        entryMap.put("block_y", loc::getBlockY);
        entryMap.put("block_z", loc::getBlockZ);

        return Utils.serializeFromMap(entryMap, path);
    }
}