package de.satsuya.elysiumCore.events;

import de.satsuya.elysiumCore.utils.NamespaceKeys;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class TeleporterEvent implements Listener {
    @EventHandler
    public void onTeleporterUse(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return; // Not a right-click, so we stop here.
        }
        if (item.getType() == Material.ENDER_PEARL) {
            if (item.getItemMeta().getPersistentDataContainer().get(NamespaceKeys.Teleporter, PersistentDataType.BOOLEAN) == Boolean.TRUE) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                event.getPlayer().teleport(event.getPlayer().getWorld().getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
            }
        }
    }
}
