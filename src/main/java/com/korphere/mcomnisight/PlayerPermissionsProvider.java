package com.korphere.mcomnisight;

import com.google.gson.JsonObject;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.korphere.mcomnisight.StatusData.plugin;

public class PlayerPermissionsProvider {
    public static @Nullable JsonObject getPlayerPermissions(@NotNull Player player) {
        if (!plugin.getConfig().getBoolean("features.players.players_list.player_data.permissions.enabled", true)) {
            return null;
        }
        String path = "features.players.players_list.player_data.permissions.";

        Map<String, Supplier<Object>> entryMap = new LinkedHashMap<>();

        entryMap.put("allow_flight", player::getAllowFlight);

        return Utils.serializeFromMap(entryMap, path);
    }
}
