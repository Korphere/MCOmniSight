package com.korphere.mcomnisight.listener;

import com.google.gson.JsonObject;
import com.korphere.mcomnisight.ConfigManager;
import com.korphere.mcomnisight.MCOmniSightVelocity;
import com.korphere.mcomnisight.provider.EventProvider;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class VOmniSightEventListener {

    private final MCOmniSightVelocity plugin;
    private final String path = "features.events.types.";
    private final PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();

    public VOmniSightEventListener(MCOmniSightVelocity plugin) {
        this.plugin = plugin;
    }

    private void dispatch(String key, JsonObject packet) {
        ConfigManager config = plugin.getConfigManager();
        if (config.getBoolean(path + key, false)) {
            if (plugin.getWsServer() != null) {
                plugin.getWsServer().broadcast(packet.toString());
            }
        }
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        if (!plugin.getConfigManager().getBoolean(path + "player_pre_login", false)) return;

        Player player = event.getPlayer();
        JsonObject packet = EventProvider.createEventPacket("PLAYER_PRE_LOGIN");
        JsonObject payload = packet.getAsJsonObject("payload");
        payload.addProperty("message", player.getUsername() + "が接続を試行しています");
        payload.addProperty("player", player.getUsername());
        payload.addProperty("uuid", player.getUniqueId().toString());
        payload.addProperty("ip_address", player.getRemoteAddress().getAddress().getHostAddress());
        payload.addProperty("result", event.getResult().isAllowed() ? "ALLOWED" : "DENIED");

        dispatch("v_player_pre_login", packet);
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        if (!plugin.getConfigManager().getBoolean(path + "player_join", false)) return;

        Player player = event.getPlayer();
        JsonObject packet = EventProvider.createEventPacket("PLAYER_JOIN");
        JsonObject payload = packet.getAsJsonObject("payload");
        payload.addProperty("message", player.getUsername() + "が参加しました");
        payload.addProperty("player", player.getUsername());
        payload.addProperty("uuid", player.getUniqueId().toString());
        payload.addProperty("display_name", player.getUsername());

        String currentServer = player.getCurrentServer()
                .map(sv -> sv.getServerInfo().getName())
                .orElse("Unknown");
        payload.addProperty("server", currentServer);

        dispatch("v_player_join", packet);
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        if (!plugin.getConfigManager().getBoolean(path + "player_quit", false)) return;

        Player player = event.getPlayer();
        JsonObject packet = EventProvider.createEventPacket("PLAYER_QUIT");
        JsonObject payload = packet.getAsJsonObject("payload");
        payload.addProperty("message", player.getUsername() + "が退出しました");
        payload.addProperty("player", player.getUsername());
        payload.addProperty("uuid", player.getUniqueId().toString());
        payload.addProperty("display_name", player.getUsername());

        String currentServer = player.getCurrentServer()
                .map(sv -> sv.getServerInfo().getName())
                .orElse("Unknown");
        payload.addProperty("server", currentServer);
        payload.addProperty("reason", event.getLoginStatus().name());

        dispatch("v_player_quit", packet);
    }

    @Subscribe
    public void onChat(PlayerChatEvent event) {
        if (!plugin.getConfigManager().getBoolean(path + "player_chat", false)) return;

        Player player = event.getPlayer();
        JsonObject packet = EventProvider.createEventPacket("PLAYER_CHAT");
        JsonObject payload = packet.getAsJsonObject("payload");
        payload.addProperty("message", event.getMessage());
        payload.addProperty("player", player.getUsername());
        payload.addProperty("uuid", player.getUniqueId().toString());
        payload.addProperty("display_name", player.getUsername());

        String currentServer = player.getCurrentServer()
                .map(sv -> sv.getServerInfo().getName())
                .orElse("Unknown");
        payload.addProperty("server", currentServer);

        dispatch("v_player_chat", packet);
    }
}