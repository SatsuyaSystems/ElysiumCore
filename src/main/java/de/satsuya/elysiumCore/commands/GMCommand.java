package de.satsuya.elysiumCore.commands;

import de.satsuya.elysiumCore.interfaces.PluginCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GMCommand implements PluginCommand {
    @Override
    public String getName() {
        return "gm";
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }
        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("elysiumcore.gm.notify")) {
                onlinePlayer.sendMessage(ChatColor.RED + ((Player) sender).getPlayer().getDisplayName() + " benötigt einen Gamemaster!");
                onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 3F, 1F);
            }
        }
        sender.sendMessage(ChatColor.GREEN + "Du hast einen Gamemaster gerufen. Bitte warte einen Augenblick.");
        return true;
    }
}
