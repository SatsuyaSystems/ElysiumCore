package de.satsuya.elysiumCore.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InvClickEvent implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getView().getTitle().equals("Roulette")) {
            event.setCancelled(true);
        }
        if (event.getView().getTitle().equals("Elysium Inventory")) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            player.getInventory().addItem(item);
        }
    }
}
