package com.korphere.mcomnisight;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.korphere.mcomnisight.StatusData.plugin;

public class StandardDataProvider {
    public static @Nullable JsonObject getStandardData() {
        if (!plugin.getConfig().getBoolean("features.standard.enabled", true)) {
            return null;
        }
        String path = "features.standard.";
        Map<String, Supplier<Object>> entryMap = new LinkedHashMap<>();entryMap.put("online_players", () -> Bukkit.getOnlinePlayers().size());
        entryMap.put("max_players", Bukkit::getMaxPlayers);
        entryMap.put("average_tick_time", Bukkit::getAverageTickTime);
        entryMap.put("allow_end", Bukkit::getAllowEnd);
        entryMap.put("allow_flight", Bukkit::getAllowFlight);
        entryMap.put("allow_nether", Bukkit::getAllowNether);
        entryMap.put("bukkit_version", Bukkit::getBukkitVersion);
        entryMap.put("connection_throttle", Bukkit::getConnectionThrottle);
        entryMap.put("current_tick", Bukkit::getCurrentTick);
        entryMap.put("gen_structures", Bukkit::getGenerateStructures);
        entryMap.put("hide_online_players", Bukkit::getHideOnlinePlayers);
        entryMap.put("idle_timeout", Bukkit::getIdleTimeout);
        entryMap.put("ip", Bukkit::getIp);
        entryMap.put("max_chained_neighbor_updates", Bukkit::getMaxChainedNeighborUpdates);
        entryMap.put("max_world_size", Bukkit::getMaxWorldSize);
        entryMap.put("mc_version", Bukkit::getMinecraftVersion);
        entryMap.put("name", Bukkit::getName);
        entryMap.put("online_mode", Bukkit::getOnlineMode);
        entryMap.put("port", Bukkit::getPort);
        entryMap.put("resource_pack", Bukkit::getResourcePack);
        entryMap.put("resource_pack_hash", Bukkit::getResourcePackHash);
        entryMap.put("resource_pack_prompt", Bukkit::getResourcePackPrompt);
        entryMap.put("simulation_distance", Bukkit::getSimulationDistance);
        entryMap.put("spawn_radius", Bukkit::getSpawnRadius);
        entryMap.put("update_folder", Bukkit::getUpdateFolder);
        entryMap.put("version", Bukkit::getVersion);
        entryMap.put("version_message", Bukkit::getVersionMessage);
        entryMap.put("view_distance", Bukkit::getViewDistance);
        entryMap.put("has_whitelist", Bukkit::hasWhitelist);
        entryMap.put("is_accepting_transfers", Bukkit::isAcceptingTransfers);
        entryMap.put("is_enforcing_secure_profiles", Bukkit::isEnforcingSecureProfiles);
        entryMap.put("is_hardcore", Bukkit::isHardcore);
        entryMap.put("is_logging_ips", Bukkit::isLoggingIPs);
        entryMap.put("is_primary_thread", Bukkit::isPrimaryThread);
        entryMap.put("is_resource_pack_required", Bukkit::isResourcePackRequired);
        entryMap.put("is_stopping", Bukkit::isStopping);
        entryMap.put("is_ticking_worlds", Bukkit::isTickingWorlds);
        entryMap.put("is_whitelist_enforced", Bukkit::isWhitelistEnforced);
        entryMap.put("banned_players", () -> {
            JsonArray array = new JsonArray();
            for (OfflinePlayer bannedPlayer : Bukkit.getBannedPlayers()) {
                array.add(bannedPlayer.getUniqueId().toString());
            }
            return array;
        });
        entryMap.put("motd_plain", () -> PlainTextComponentSerializer.plainText().serialize(Bukkit.motd()));
        entryMap.put("motd_json", () -> GsonComponentSerializer.gson().serialize(Bukkit.motd()));
        entryMap.put("motd_legacy", () -> LegacyComponentSerializer.legacySection().serialize(Bukkit.motd()));

        return Utils.serializeFromMap(entryMap, path);
    }
}
