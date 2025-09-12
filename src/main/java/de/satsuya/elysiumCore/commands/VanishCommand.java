package de.satsuya.elysiumCore.commands;

import de.satsuya.elysiumCore.ElysiumCore;
import de.satsuya.elysiumCore.interfaces.PluginCommand;
import de.satsuya.elysiumCore.utils.SetHolder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@PluginCommand(name = "vanish")
public class VanishCommand implements CommandExecutor, TabExecutor {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            // Nur Spielernamen vorschlagen, wenn der Unterbefehl dies erlaubt
            //if ("pay".equalsIgnoreCase(args[0]) || "balance".equalsIgnoreCase(args[0])) {
            List<String> names = new ArrayList<>();
            sender.getServer().getOnlinePlayers().forEach(pl -> names.add(pl.getName()));
            return names;
            //}
        }
        return new ArrayList<>();
    }
    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl ist nur für Spieler!");
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 1) {
            Player targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer != null && sender.hasPermission("elysiumcore.vanish.other")) {
                player = targetPlayer;
            } else {
                sender.sendMessage("Du hast nicht die Berechtigung, andere Spieler zu 'vanishen' oder der Spieler ist offline.");
                return true;
            }
        }

        if (SetHolder.vanishedPlayers.contains(player.getUniqueId())) {
            // Spieler wieder sichtbar machen
            SetHolder.vanishedPlayers.remove(player.getUniqueId());
            for (Player other : Bukkit.getOnlinePlayers()) {
                other.showPlayer(ElysiumCore.getInstance(), player);
                if (other.hasPermission("elysiumcore.vanish.see") && !other.equals(player)) {
                    other.sendMessage(player.getDisplayName() + " ist nun wieder sichtbar.");
                }
            }
            player.sendMessage("Du bist nun wieder sichtbar.");
        } else {
            // Spieler unsichtbar machen
            SetHolder.vanishedPlayers.add(player.getUniqueId());
            for (Player other : Bukkit.getOnlinePlayers()) {
                // Verstecke den Spieler nur, wenn der andere Spieler NICHT die Berechtigung hat, ihn zu sehen.
                if (!other.hasPermission("elysiumcore.vanish.see")) {
                    other.hidePlayer(ElysiumCore.getInstance(), player);
                }
                if (other.hasPermission("elysiumcore.vanish.see") && !other.equals(player)) {
                    other.sendMessage(player.getDisplayName() + " ist nun unsichtbar.");
                }
            }
            player.sendMessage("Du bist nun unsichtbar.");
        }
        return true;
    }
}
