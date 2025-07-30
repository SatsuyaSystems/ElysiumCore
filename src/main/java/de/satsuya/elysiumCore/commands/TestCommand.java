package de.satsuya.elysiumCore.commands;

public class TestCommand implements PluginCommand {

    @Override
    public String getName() {
        return "test"; // The name of the command
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        // Handle the command logic here
        if (args.length == 0) {
            sender.sendMessage("This is a test command!");
            return true;
        } else {
            sender.sendMessage("Usage: /test");
            return false;
        }
    }
}
