package org.bukkit.command.defaults;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class PluginsCommand extends BukkitCommand {
    public PluginsCommand(@NotNull String name) {
        super(name);
        this.description = "Gets a list of plugins running on the server";
        this.usageMessage = "/plugins";
        this.setPermission("bukkit.command.plugins");
        this.setAliases(Arrays.asList("pl"));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String currentAlias, @NotNull String[] args) {
        if (!testPermission(sender)) return true;

        sender.sendMessage("Plugins " + getPluginList());
        return true;
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return Collections.emptyList();
    }

    @NotNull
    private String getPluginList() {
        // Paper start
        TreeMap<String, Plugin> plugins = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            plugins.put(plugin.getDescription().getName(), plugin);
        }

        StringBuilder pluginList = new StringBuilder();
        for (Map.Entry<String, Plugin> entry : plugins.entrySet()) {
            if (pluginList.length() > 0) {
                pluginList.append(ChatColor.WHITE);
                pluginList.append(", ");
            }

            Plugin plugin = entry.getValue();

            if (plugin.getDescription().getProvides().size() > 0) {
                pluginList.append(" (").append(String.join(", ", plugin.getDescription().getProvides())).append(")");
            }


            pluginList.append(plugin.isEnabled() ? ChatColor.GREEN : ChatColor.RED);
            // Paper start - Add an asterisk to legacy plugins (so admins are aware)
            String pluginName = plugin.getDescription().getName();
            if (org.bukkit.UnsafeValues.isLegacyPlugin(plugin)) {
                pluginName += "*";
            }
            pluginList.append(pluginName);
            // Paper end
        }

        return "(" + plugins.size() + "): " + pluginList.toString();
        // Paper end
    }
}
