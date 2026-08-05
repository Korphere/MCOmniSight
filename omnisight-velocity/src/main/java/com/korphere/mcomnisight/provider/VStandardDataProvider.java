package com.korphere.mcomnisight.provider;

import com.google.gson.JsonObject;
import com.korphere.mcomnisight.UtilsVelocity;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.util.Favicon;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.korphere.mcomnisight.StatusData.plugin;

public class VStandardDataProvider {
    public static @Nullable JsonObject getStandardData() {
        if (!plugin.getConfigManager().getBoolean("features.standard_v.enabled", true)) {
            return null;
        }
        String path = "features.standard_v.";

        ProxyServer server = plugin.getServer();
        var config = server.getConfiguration();

        Map<String, Supplier<Object>> entryMap = new LinkedHashMap<>();

        entryMap.put("online_players", server::getPlayerCount);
        entryMap.put("max_players", config::getShowMaxPlayers);

        entryMap.put("name", () -> server.getVersion().getName());
        entryMap.put("version", () -> server.getVersion().getVersion());
        entryMap.put("vendor", () -> server.getVersion().getVendor());

        entryMap.put("ip", () -> server.getBoundAddress().getAddress().getHostAddress());
        entryMap.put("port", () -> server.getBoundAddress().getPort());
        entryMap.put("online_mode", config::isOnlineMode);

        entryMap.put("servers_count", () -> server.getAllServers().size());

        entryMap.put("favicon", () -> config.getFavicon().map(Favicon::getBase64Url).orElse(""));

        entryMap.put("motd_plain", () -> PlainTextComponentSerializer.plainText().serialize(config.getMotd()));
        entryMap.put("motd_json", () -> GsonComponentSerializer.gson().serialize(config.getMotd()));
        entryMap.put("motd_legacy", () -> LegacyComponentSerializer.legacySection().serialize(config.getMotd()));

        return UtilsVelocity.serializeFromMap(entryMap, path);
    }
}