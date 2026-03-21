/*
 * TODO: Due to the wide variety of events, we plan to add new ones with each update.
 */

package com.korphere.mcomnisight;

import com.google.gson.JsonObject;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.jetbrains.annotations.NotNull;

public class OmniSightEventListener implements Listener {
    private final MCOmniSight plugin;
    private final String path = "features.events.types.";
    private final PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();

    public OmniSightEventListener(MCOmniSight plugin) {
        this.plugin = plugin;
    }

    private void dispatch(String key, JsonObject packet) {
        if (plugin.getConfig().getBoolean(path + key, false)) {
            if (plugin.getWsServer() != null) {
                plugin.getWsServer().broadcast(packet.toString());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(@NotNull PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean(path + "player_join", false)) return;
        JsonObject packet = EventProvider.createEventPacket("PLAYER_JOIN",
                event.getPlayer().getName() + "が参加しました");
        packet.addProperty("player", event.getPlayer().getName());
        packet.addProperty("uuid", event.getPlayer().getUniqueId().toString());
        packet.addProperty("display_name", plainSerializer.serialize(event.getPlayer().displayName()));
        packet.addProperty("world", event.getPlayer().getWorld().getName());
        dispatch("player_join", packet);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(@NotNull PlayerQuitEvent event) {
        if (!plugin.getConfig().getBoolean(path + "player_quit", false)) return;
        JsonObject packet = EventProvider.createEventPacket("PLAYER_QUIT",
                event.getPlayer().getName() + "が退出しました");
        packet.addProperty("player", event.getPlayer().getName());
        packet.addProperty("uuid", event.getPlayer().getUniqueId().toString());
        packet.addProperty("display_name", plainSerializer.serialize(event.getPlayer().displayName()));
        packet.addProperty("world", event.getPlayer().getWorld().getName());
        packet.addProperty("reason", event.getReason().name());
        dispatch("player_quit", packet);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAnimation(@NotNull PlayerAnimationEvent event) {
        if (!plugin.getConfig().getBoolean(path + "player_animation", false)) return;
        JsonObject packet = EventProvider.createEventPacket("PLAYER_ANIMATION",
                event.getPlayer().getName() + "がアクションを実行しました");
        packet.addProperty("player", event.getPlayer().getName());
        packet.addProperty("uuid", event.getPlayer().getUniqueId().toString());
        packet.addProperty("display_name", plainSerializer.serialize(event.getPlayer().displayName()));
        packet.addProperty("world", event.getPlayer().getWorld().getName());
        packet.addProperty("anim_type", event.getAnimationType().name());

        dispatch("player_animation", packet);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(@NotNull AsyncChatEvent event) {
        if (!plugin.getConfig().getBoolean(path + "player_chat", false)) return;
        JsonObject packet = EventProvider.createEventPacket("PLAYER_CHAT",
                PlainTextComponentSerializer.plainText().serialize(event.message()));
        packet.addProperty("player", event.getPlayer().getName());
        packet.addProperty("uuid", event.getPlayer().getUniqueId().toString());
        packet.addProperty("display_name", plainSerializer.serialize(event.getPlayer().displayName()));
        packet.addProperty("world", event.getPlayer().getWorld().getName());

        dispatch("player_chat", packet);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLogin(@NotNull AsyncPlayerPreLoginEvent event) {
        if (!plugin.getConfig().getBoolean(path + "player_pre_login", false)) return;
        JsonObject packet = EventProvider.createEventPacket("PLAYER_PRE_LOGIN",
                event.getName() + "が接続を試行しています");
        packet.addProperty("player", event.getName());
        packet.addProperty("uuid", event.getUniqueId().toString());
        packet.addProperty("ip_address", event.getAddress().getHostAddress());
        packet.addProperty("result", event.getLoginResult().name());

        dispatch("player_pre_login", packet);
    }
}