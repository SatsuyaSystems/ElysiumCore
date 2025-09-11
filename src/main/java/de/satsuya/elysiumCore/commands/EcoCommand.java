package de.satsuya.elysiumCore.commands;

import de.satsuya.elysiumCore.ElysiumCore;
import de.satsuya.elysiumCore.interfaces.PluginCommand;
import de.satsuya.elysiumCore.manager.EcoManager;
import de.satsuya.elysiumCore.utils.ManagerRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EcoCommand implements PluginCommand, TabExecutor {
    private final EcoManager ecoManager;

    public EcoCommand() {
        this.ecoManager = ManagerRegistry.get("eco");
    }

    @Override
    public String getName() {
        return "eco";
    }
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1)
            return Arrays.asList("balance", "pay", "set", "add");
        if (args.length == 2) {
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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            showHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "pay":
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        handlePayCommand(player, args);
                    }
                }.runTaskAsynchronously(ElysiumCore.getInstance());
                break;
            case "balance":
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        handleBalanceCommand(player, args);
                    }
                }.runTaskAsynchronously(ElysiumCore.getInstance());
                break;
            case "add":
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        handleAddCommand(player, args);
                    }
                }.runTaskAsynchronously(ElysiumCore.getInstance());
                break;
            case "set":
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        handleSetCommand(player, args);
                    }
                }.runTaskAsynchronously(ElysiumCore.getInstance());
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unbekannter Unterbefehl. Nutze /" + label + " help für eine Liste der Befehle.");
                break;
        }
        return true;
    }

    private void showHelp(Player player) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a----- Economy Befehle -----"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/eco balance [Spieler] &7- Zeigt den aktuellen Kontostandt."));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/eco pay <Spieler> <Anzahl> &7- Überträgt Geld auf ein anderes Spielerkonto."));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/eco add <Spieler> <Anzahl> &7- Fügt Geld auf einem Spielerkonto hinzu."));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/eco set <Spieler> <Anzahl> &7- Setzt Geld auf einem Spielerkonto."));
    }

    private void handlePayCommand(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Nutzung: /eco pay <Spieler> <Anzahl>");
            return;
        }
        Player targetPlayer = Bukkit.getPlayer(args[1]);
        if (targetPlayer == null) {
            player.sendMessage(ChatColor.RED + "Der Spieler ist nicht online oder existiert nicht.");
            return;
        }
        Integer amount = Integer.valueOf(args[2]);
        if (!ecoManager.hasValidBalance(player, amount)) {
            player.sendMessage(ChatColor.RED + "Du hast nicht genug COL um das zu tun.");
            return;
        }
        ecoManager.transfere(player, targetPlayer, amount);
        player.sendMessage(ChatColor.GREEN + "Du hast " + amount + "COL an " + targetPlayer.getDisplayName() + " gesendet.");
        targetPlayer.sendMessage(ChatColor.GREEN + "Du hast " + amount + "COL von " + player.getDisplayName() + " bekommen.");
    }

    private void handleBalanceCommand(Player player, String[] args) {
        // /eco balance -> eigener Kontostand
        if (args.length == 1) {
            Integer balance = ecoManager.getPlayerBalance(player);
            player.sendMessage(ChatColor.GREEN + "Du hast " + balance + "COL.");
            return;
        }

        // /eco balance <Spieler> -> Kontostand eines anderen Spielers
        if (args.length >= 2) {
            if (!player.hasPermission("elysiumcore.admin.eco")) {
                player.sendMessage(ChatColor.RED + "Du darfst nicht den Kontostand von anderen sehen.");
                return;
            }
            Player targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null) {
                player.sendMessage(ChatColor.RED + "Der angegebene Spieler ist nicht online oder existiert nicht.");
                return;
            }
            Integer balance = ecoManager.getPlayerBalance(targetPlayer);
            player.sendMessage(ChatColor.GREEN + targetPlayer.getDisplayName() + " hat " + balance + "COL.");
        }
    }

    private void handleAddCommand(Player player, String[] args) {
        if (!player.hasPermission("elysiumcore.admin.eco")) {
            player.sendMessage(ChatColor.RED + "Du darfst nicht den Kontostand von anderen sehen.");
            return;
        }
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Nutzung: /eco add <Spieler> <Anzahl>");
            return;
        }
        Player targetPlayer = Bukkit.getPlayer(args[1]);
        if (targetPlayer == null) {
            player.sendMessage(ChatColor.RED + "Der angegebene Spieler ist nicht online oder existiert nicht.");
            return;
        }
        Integer amount = Integer.valueOf(args[2]);
        ecoManager.addBalance(targetPlayer, amount);
        player.sendMessage(ChatColor.GREEN + targetPlayer.getDisplayName() + " wurden " + amount + "COL hinzugefügt.");
        targetPlayer.sendMessage(ChatColor.GREEN + "Dir wurden " + amount + "COL hinzugefügt");
    }

    private void handleSetCommand(Player player, String[] args) {
        if (!player.hasPermission("elysiumcore.admin.eco")) {
            player.sendMessage(ChatColor.RED + "Du darfst nicht den Kontostand von anderen sehen.");
            return;
        }
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Nutzung: /eco set <Spieler> <Anzahl>");
            return;
        }
        Player targetPlayer = Bukkit.getPlayer(args[1]);
        if (targetPlayer == null) {
            player.sendMessage(ChatColor.RED + "Der angegebene Spieler ist nicht online oder existiert nicht.");
            return;
        }
        Integer amount = Integer.valueOf(args[2]);
        ecoManager.setBalance(targetPlayer, amount);
        player.sendMessage(ChatColor.GREEN + targetPlayer.getDisplayName() + " wurde auf " + amount + "COL gesetzt.");
        targetPlayer.sendMessage(ChatColor.GREEN + "Du wurdest auf " + amount + "COL gesetzt");
    }

}
