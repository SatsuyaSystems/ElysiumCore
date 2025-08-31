package de.satsuya.elysiumCore.commands;

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

public class SpeedCommand implements PluginCommand, TabExecutor {
    @Override
    public String getName() {
        return "speed";
    }
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1)
            return Arrays.asList("walk", "fly");
        return new ArrayList<>();
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Überprüfen, ob der Sender ein Spieler ist
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        Player player = (Player) sender;

        // Überprüfen, ob genügend Argumente vorhanden sind
        if (args.length == 2) {
            // Überprüfen, ob es sich um 'walk' oder 'fly' Geschwindigkeit handelt
            if (args[0].equalsIgnoreCase("walk") || args[0].equalsIgnoreCase("fly")) {
                try {
                    // Den Geschwindigkeitswert parsen
                    int speed = Integer.parseInt(args[1]);

                    // Geschwindigkeit auf den Bereich von 1 bis 10 begrenzen
                    if (speed >= 1 && speed <= 10) {
                        // Den Wert in ein Float umwandeln und durch 10 teilen, um den korrekten Minecraft-Wert zu erhalten
                        float finalSpeed = (float) speed / 10;

                        if (args[0].equalsIgnoreCase("walk")) {
                            player.setWalkSpeed(finalSpeed);
                            player.sendMessage("Deine Laufgeschwindigkeit wurde auf " + speed + " gesetzt.");
                        } else { // fly
                            // Sicherstellen, dass der Spieler fliegen kann, bevor die Fluggeschwindigkeit geändert wird
                            if (!player.getAllowFlight()) {
                                player.setAllowFlight(true);
                            }
                            player.setFlySpeed(finalSpeed);
                            player.sendMessage("Deine Fluggeschwindigkeit wurde auf " + speed + " gesetzt.");
                        }
                    } else {
                        player.sendMessage("Der Geschwindigkeitswert muss zwischen 1 und 10 liegen.");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("Ungültiger Geschwindigkeitswert. Bitte gib eine Zahl ein.");
                }
            } else {
                player.sendMessage("Ungültiger Geschwindigkeitstyp. Verwende 'walk' oder 'fly'.");
            }
        } else {
            player.sendMessage("Verwendung: /speed <walk|fly> <wert>");
        }

        return true;
    }
}
