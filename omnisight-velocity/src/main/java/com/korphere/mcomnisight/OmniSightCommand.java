package com.korphere.mcomnisight;

import com.korphere.mcomnisight.server.OmniSightServer;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OmniSightCommand implements SimpleCommand {

    private final MCOmniSightVelocity plugin;

    public OmniSightCommand(MCOmniSightVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            sendHelp(sender);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("omnisight.admin")) {
                    sender.sendMessage(Component.text("You do not have permission.", NamedTextColor.RED));
                    return;
                }
                plugin.reloadPlugin();
                sender.sendMessage(Component.text("✔ The MCOmniSight settings have been reloaded.", NamedTextColor.GREEN));
                break;

            case "status":
                if (!sender.hasPermission("omnisight.admin")) {
                    sender.sendMessage(Component.text("You do not have permission.", NamedTextColor.RED));
                    return;
                }

                ConfigManager config = plugin.getConfigManager();
                int port = config.getWebSocketPort();

                OmniSightServer wsServer = plugin.getWsServer();
                int clients = (wsServer != null) ? wsServer.getConnectedClientsCount() : 0;

                sender.sendMessage(Component.text("--- MCOmniSight Status ---", NamedTextColor.AQUA));
                sender.sendMessage(Component.text("➤ WebSocket Port: " + port, NamedTextColor.WHITE));
                sender.sendMessage(Component.text("➤ Active Clients: " + clients, NamedTextColor.GREEN));
                sender.sendMessage(Component.text("➤ Gzip Enabled: " + config.isGzipEnabled(), NamedTextColor.YELLOW));
                sender.sendMessage(Component.text("➤ Whitelist Enabled: " + config.isWhitelistEnabled(), NamedTextColor.YELLOW));
                break;

            default:
                sendHelp(sender);
                break;
        }
    }

    private void sendHelp(CommandSource sender) {
        sender.sendMessage(Component.text("Usage: /omnisight <reload|status>", NamedTextColor.YELLOW));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length <= 1) {
            List<String> completions = List.of("reload", "status");
            String current = args.length == 1 ? args[0].toLowerCase() : "";

            return completions.stream()
                    .filter(s -> s.startsWith(current))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("omnisight.admin");
    }
}