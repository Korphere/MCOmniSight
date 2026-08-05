package com.korphere.mcomnisight.provider;

import com.google.gson.JsonObject;
import com.korphere.mcomnisight.StatusData;
import com.korphere.mcomnisight.UtilsPaper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PlayerPermissionsProvider {
    public static @Nullable JsonObject getPlayerPermissions(@NotNull Player player) {
        if (!StatusData.plugin.getConfig().getBoolean("features.players.players_list.player_data.permissions.enabled", true)) {
            return null;
        }
        String path = "features.players.players_list.player_data.permissions.";

        Map<String, Supplier<Object>> entryMap = new LinkedHashMap<>();

        entryMap.put("allow_flight", player::getAllowFlight);

        return UtilsPaper.serializeFromMap(entryMap, path);
    }
}
