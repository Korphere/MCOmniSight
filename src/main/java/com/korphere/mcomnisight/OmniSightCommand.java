package com.korphere.mcomnisight;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.korphere.mcomnisight.StatusData.plugin;

public class OmniSightCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("omnisight.admin")) {
                    sender.sendMessage(Component.text("権限がありません。", NamedTextColor.RED));
                    return true;
                }
                plugin.reloadConfig();
                plugin.reloadPlugin();
                sender.sendMessage(Component.text("✔ MCOmniSight の設定をリロードしました。", NamedTextColor.GREEN));
                break;

            case "status":
                if (!sender.hasPermission("omnisight.admin")) {
                    sender.sendMessage(Component.text("権限がありません。", NamedTextColor.RED));
                    return true;
                }
                int port = plugin.getConfig().getInt("websocket-port");
                int clients = plugin.getWsServer().getConnectedClientsCount();

                sender.sendMessage(Component.text("--- MCOmniSight Status ---", NamedTextColor.AQUA));
                sender.sendMessage(Component.text("➤ WebSocket Port: " + port, NamedTextColor.WHITE));
                sender.sendMessage(Component.text("➤ Active Clients: " + clients, NamedTextColor.GREEN));
                sender.sendMessage(Component.text("➤ Enabled Features:", NamedTextColor.YELLOW));

                // featuresセクションを再帰的にスキャンして表示
                org.bukkit.configuration.ConfigurationSection features = plugin.getConfig().getConfigurationSection("features");
                if (features != null) {
                    displayEnabledFeatures(sender, features, 1);
                }
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(@NotNull CommandSender sender) {
        sender.sendMessage(Component.text("Usage: /omnisight <reload|status>", NamedTextColor.YELLOW));
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("reload");
            completions.add("status");
            return completions.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private void displayEnabledFeatures(@NotNull CommandSender sender, @NotNull org.bukkit.configuration.ConfigurationSection section, int depth) {
        for (String key : section.getKeys(false)) {
            Object value = section.get(key);
            String indent = "  ".repeat(depth);

            if (value instanceof Boolean) {
                if ((Boolean) value) {
                    sender.sendMessage(Component.text(indent + "✔ " + key, NamedTextColor.GREEN));
                }
            } else if (value instanceof org.bukkit.configuration.ConfigurationSection subSection) {
                if (hasAnyEnabled(subSection)) {
                    sender.sendMessage(Component.text(indent + "📁 " + key, NamedTextColor.YELLOW));
                    displayEnabledFeatures(sender, subSection, depth + 1);
                }
            }
        }
    }

    private boolean hasAnyEnabled(@NotNull org.bukkit.configuration.ConfigurationSection section) {
        for (String key : section.getKeys(true)) {
            if (section.isBoolean(key) && section.getBoolean(key)) return true;
        }
        return false;
    }
}