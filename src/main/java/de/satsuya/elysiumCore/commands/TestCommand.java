package de.satsuya.elysiumCore.commands;

import de.satsuya.elysiumCore.interfaces.PluginCommand;
import de.satsuya.elysiumCore.loaders.ConfigLoader;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@PluginCommand(name = "test")
public class TestCommand implements CommandExecutor, TabExecutor {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }
    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        // Handle the command logic here
        if (args.length == 0) {
            sender.sendMessage("Olla " + ConfigLoader.configData.getString("test"));
            return true;
        } else {
            sender.sendMessage("Usage: /test");
            return false;
        }
    }
}
