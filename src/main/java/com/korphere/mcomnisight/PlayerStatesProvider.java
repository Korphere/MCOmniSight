package com.korphere.mcomnisight;

import com.google.gson.JsonObject;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.korphere.mcomnisight.StatusData.plugin;

public class PlayerStatesProvider {
    public static @Nullable JsonObject getPlayerStates(@NotNull Player player) {
        if (!plugin.getConfig().getBoolean("features.players.players_list.player_data.states.enabled", true)) {
            return null;
        }
        String path = "features.players.players_list.player_data.states.";

        Map<String, Supplier<Object>> entryMap = new LinkedHashMap<>();
        
        entryMap.put("inside_vehicle", player::isInsideVehicle);
        entryMap.put("gliding", player::isGliding);
        entryMap.put("glowing", player::isGlowing);
        entryMap.put("swimming", player::isSwimming);
        entryMap.put("sleeping", player::isSleeping);
        entryMap.put("flying", player::isFlying);
        entryMap.put("sneaking", player::isSneaking);
        entryMap.put("sprinting", player::isSprinting);
        entryMap.put("blocking", player::isBlocking);
        entryMap.put("climbing", player::isClimbing);
        entryMap.put("conversing", player::isConversing);
        entryMap.put("deeply_sleeping", player::isDeeplySleeping);
        entryMap.put("frozen", player::isFrozen);
        entryMap.put("sleeping_ignored", player::isSleepingIgnored);
        entryMap.put("banned", player::isBanned);
        entryMap.put("dead", player::isDead);
        entryMap.put("hand_raised", player::isHandRaised);
        entryMap.put("in_lava", player::isInLava);
        entryMap.put("in_water", player::isInWater);
        entryMap.put("collidable", player::isCollidable);

        return Utils.serializeFromMap(entryMap, path);
    }
}
