package de.satsuya.elysiumCore.commands;

import de.satsuya.elysiumCore.ElysiumCore;
import de.satsuya.elysiumCore.interfaces.PluginCommand;
import de.satsuya.elysiumCore.manager.InventoryManager;
import de.satsuya.elysiumCore.utils.ManagerRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@PluginCommand(name = "reboot")
public class RebootCommand implements CommandExecutor, TabExecutor {
    private final InventoryManager inventoryManager;
    public RebootCommand() {
        this.inventoryManager = ManagerRegistry.get("inventory");
    }
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Ensure this method is thread-safe or handles its own threads.
            player.sendMessage(ChatColor.RED + "SERVER RESTART IN 5 SEKUNDEN");
        }
        new BukkitRunnable() {
            int countdown = 5;
            @Override
            public void run() {
                if (countdown > 0) {
                    // Send the title message
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title @a title {\"text\":\"" + countdown + " seconds" + "\", \"color\":\"red\"}");
                    countdown--;
                } else {
                    // Final title before shutdown


                    // Save player data on the main thread
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        // Ensure this method is thread-safe or handles its own threads.
                        inventoryManager.saveInventory(player);
                    }

                    // Stop the server
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");

                    // Cancel the task once finished
                    this.cancel();
                }
            }
        }.runTaskTimer(ElysiumCore.getInstance(), 20L, 20L); // Delay 1s, Repeat every 1s
        return true;
    }
}
