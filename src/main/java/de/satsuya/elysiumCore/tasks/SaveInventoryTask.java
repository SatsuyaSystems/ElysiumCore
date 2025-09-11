package de.satsuya.elysiumCore.tasks;

import de.satsuya.elysiumCore.interfaces.TaskSchedule;
import de.satsuya.elysiumCore.manager.InventoryManager;
import de.satsuya.elysiumCore.utils.ElysiumLogger;
import de.satsuya.elysiumCore.utils.ManagerRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@TaskSchedule(delay = 0L, period = 20L * 60L * 5L, async = true)
public class SaveInventoryTask extends BukkitRunnable {
    @Override
    public void run() {
        final InventoryManager inventoryManager = ManagerRegistry.get("inventory");
        for (Player player : Bukkit.getOnlinePlayers()) {
            inventoryManager.saveInventory(player);
            ElysiumLogger.log("Saved inventory for " + player.getName() + " to the database.");
            player.sendMessage(ChatColor.GREEN + "Dein Inventar wurde gespeichert! (Dies passiert alle 5 min)");
        }
    }
}
