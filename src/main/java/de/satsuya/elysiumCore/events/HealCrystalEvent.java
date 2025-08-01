package de.satsuya.elysiumCore.events;

import de.satsuya.elysiumCore.utils.NamespaceKeys;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HealCrystalEvent implements Listener {
    @EventHandler
    public void onHealCrystalUse(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return; // Not a right-click, so we stop here.
        }
        if (item == null) {
            return;
        }
        if (item.getType() == Material.AMETHYST_SHARD) {
            if (item.getItemMeta().getPersistentDataContainer().get(NamespaceKeys.HealCrystal, PersistentDataType.BOOLEAN) == Boolean.TRUE) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20, 255));
            }
        }
    }
}
