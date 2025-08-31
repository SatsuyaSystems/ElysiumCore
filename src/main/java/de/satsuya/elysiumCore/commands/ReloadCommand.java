package de.satsuya.elysiumCore.commands;

import de.satsuya.elysiumCore.utils.ConfigLoader;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand implements PluginCommand, TabExecutor {
    @Override
    public String getName() {
        return "reloadcore"; // The name of the command
    }
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }
    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        // Handle the reload logic here
        if (args.length == 0) {
            // Reload the plugin configuration or other necessary components
            ConfigLoader.reloadConfig();
            sender.sendMessage("ElysiumCore has been reloaded successfully.");
            return true;
        } else {
            sender.sendMessage("Usage: /reloadcore");
            return false;
        }
    }
}
