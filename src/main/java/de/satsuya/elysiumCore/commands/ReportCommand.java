package de.satsuya.elysiumCore.commands;

import de.satsuya.elysiumCore.interfaces.PluginCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReportCommand implements PluginCommand, TabExecutor {
    @Override
    public String getName() {
        return "report";
    }
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1)
            return Arrays.asList("player", "bug");
        if (args.length == 2) {
            // Nur Spielernamen vorschlagen, wenn der Unterbefehl dies erlaubt
            if ("player".equalsIgnoreCase(args[0])) {
            List<String> names = new ArrayList<>();
            sender.getServer().getOnlinePlayers().forEach(pl -> names.add(pl.getName()));
            return names;
            }
        }

        return new ArrayList<>();
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }
        String type = args[0];
        String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("elysiumcore.report.notify")) {
                onlinePlayer.sendMessage(ChatColor.RED + ((Player) sender).getPlayer().getDisplayName() + " reportet einen: " + type + " -> " + reason);
                onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 3F, 1F);
            }
        }
        sender.sendMessage(ChatColor.GREEN + "Report wurde gesendet!");
        return true;
    }
}
