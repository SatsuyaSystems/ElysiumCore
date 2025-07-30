package de.satsuya.elysiumCore.commands;

import de.satsuya.elysiumCore.utils.ConfigLoader;

public class ReloadCommand implements PluginCommand{
    @Override
    public String getName() {
        return "reloadcore"; // The name of the command
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
