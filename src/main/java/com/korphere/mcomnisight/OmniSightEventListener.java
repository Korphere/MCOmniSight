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
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
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
        JsonObject packet = EventProvider.createEventPacket("PLAYER_JOIN");
        JsonObject payload = packet.getAsJsonObject("payload");
        payload.addProperty("message", event.getPlayer().getName() + "が参加しました");
        payload.addProperty("player", event.getPlayer().getName());
        payload.addProperty("uuid", event.getPlayer().getUniqueId().toString());
        payload.addProperty("display_name", plainSerializer.serialize(event.getPlayer().displayName()));
        payload.addProperty("world", event.getPlayer().getWorld().getName());
        dispatch("player_join", packet);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(@NotNull PlayerQuitEvent event) {
        if (!plugin.getConfig().getBoolean(path + "player_quit", false)) return;
        JsonObject packet = EventProvider.createEventPacket("PLAYER_QUIT");
        JsonObject payload = packet.getAsJsonObject("payload");
        payload.addProperty("message", event.getPlayer().getName() + "が退出しました");
        payload.addProperty("player", event.getPlayer().getName());
        payload.addProperty("uuid", event.getPlayer().getUniqueId().toString());
        payload.addProperty("display_name", plainSerializer.serialize(event.getPlayer().displayName()));
        payload.addProperty("world", event.getPlayer().getWorld().getName());
        payload.addProperty("reason", event.getReason().name());
        dispatch("player_quit", packet);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAnimation(@NotNull PlayerAnimationEvent event) {
        if (!plugin.getConfig().getBoolean(path + "player_animation", false)) return;
        JsonObject packet = EventProvider.createEventPacket("PLAYER_ANIMATION");
        JsonObject payload = packet.getAsJsonObject("payload");
        payload.addProperty("message", event.getPlayer().getName() + "がアクションを実行しました");
        payload.addProperty("player", event.getPlayer().getName());
        payload.addProperty("uuid", event.getPlayer().getUniqueId().toString());
        payload.addProperty("display_name", plainSerializer.serialize(event.getPlayer().displayName()));
        payload.addProperty("world", event.getPlayer().getWorld().getName());
        payload.addProperty("anim_type", event.getAnimationType().name());

        dispatch("player_animation", packet);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(@NotNull AsyncChatEvent event) {
        if (!plugin.getConfig().getBoolean(path + "player_chat", false)) return;
        JsonObject packet = EventProvider.createEventPacket("PLAYER_CHAT");
        JsonObject payload = packet.getAsJsonObject("payload");
        payload.addProperty("message", PlainTextComponentSerializer.plainText().serialize(event.message()));
        payload.addProperty("player", event.getPlayer().getName());
        payload.addProperty("uuid", event.getPlayer().getUniqueId().toString());
        payload.addProperty("display_name", plainSerializer.serialize(event.getPlayer().displayName()));
        payload.addProperty("world", event.getPlayer().getWorld().getName());

        dispatch("player_chat", packet);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLogin(@NotNull AsyncPlayerPreLoginEvent event) {
        if (!plugin.getConfig().getBoolean(path + "player_pre_login", false)) return;
        JsonObject packet = EventProvider.createEventPacket("PLAYER_PRE_LOGIN");
        JsonObject payload = packet.getAsJsonObject("payload");
        payload.addProperty("message", event.getName() + "が接続を試行しています");
        payload.addProperty("player", event.getName());
        payload.addProperty("uuid", event.getUniqueId().toString());
        payload.addProperty("ip_address", event.getAddress().getHostAddress());
        payload.addProperty("result", event.getLoginResult().name());

        dispatch("player_pre_login", packet);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAdvancementDone(@NotNull PlayerAdvancementDoneEvent event) {
        if (!plugin.getConfig().getBoolean(path + "player_advancement_done", false)) return;
        JsonObject packet = EventProvider.createEventPacket("PLAYER_ADVANCEMENT_DONE");
        JsonObject payload = packet.getAsJsonObject("payload");
        payload.addProperty("message", event.getPlayer().getName() + "が" + plainSerializer.serialize(event.getAdvancement().displayName()) + "を達成しました");
        payload.addProperty("player", event.getPlayer().getName());
        payload.addProperty("uuid", event.getPlayer().getUniqueId().toString());
        payload.addProperty("advancement", plainSerializer.serialize(event.getAdvancement().displayName()));

        dispatch("player_advancement_done", packet);
    }
}