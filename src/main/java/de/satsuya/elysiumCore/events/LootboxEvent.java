package de.satsuya.elysiumCore.events;

import de.satsuya.elysiumCore.ElysiumCore;
import de.satsuya.elysiumCore.utils.NamespaceKeys;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LootboxEvent implements Listener {
    private final List<Material> items = Arrays.asList(
            // Erze & Ressourcen
            Material.DIAMOND, Material.GOLD_INGOT, Material.IRON_INGOT, Material.EMERALD,
            Material.REDSTONE, Material.LAPIS_LAZULI, Material.COAL, Material.COPPER_INGOT,
            // Essen
            Material.BREAD, Material.APPLE, Material.COOKED_BEEF, Material.COOKED_CHICKEN,
            Material.COOKED_PORKCHOP, Material.CARROT, Material.POTATO, Material.BAKED_POTATO,
            Material.COOKED_MUTTON, Material.COOKED_RABBIT, Material.COOKED_COD, Material.COOKED_SALMON,
            Material.MELON_SLICE,
            // Mobdrops
            Material.BONE, Material.ROTTEN_FLESH, Material.STRING, Material.SPIDER_EYE,
            Material.GUNPOWDER, Material.LEATHER, Material.FEATHER, Material.ENDER_PEARL,
            Material.SLIME_BALL
    );

    @EventHandler
    public void onLootboxPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer data = meta.getPersistentDataContainer();
            if (data.get(NamespaceKeys.LOOT_BOX.getKey(), PersistentDataType.BOOLEAN) == Boolean.TRUE) {
                event.setCancelled(true);
                openRoulette(player);
                // Entferne die Lootbox aus dem Inventar des Spielers
                item.setAmount(item.getAmount() - 1);
                player.getInventory().setItemInMainHand(item);
            }
        }
    }

    private void openRoulette(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Roulette");
        fillInventory(inv);
        player.openInventory(inv);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                fillInventory(inv);
                ticks++;
                if (ticks >= 20) {
                    ItemStack middle = inv.getItem(4);
                    if (middle != null) {
                        player.getInventory().addItem(middle);
                        player.sendMessage("Du hast " + middle.getType().name() + " gewonnen!");
                    }
                    cancel();
                }
            }
        }.runTaskTimer(ElysiumCore.getPlugin(ElysiumCore.class), 0L, 5L);
    }

    private void fillInventory(Inventory inv) {
        Random rand = new Random();
        for (int i = 0; i < 9; i++) {
            if (i != 4) {
                inv.setItem(i, new ItemStack(Material.RED_STAINED_GLASS_PANE)); // Mitte leer lassen
                continue;
            }
            Material mat = items.get(rand.nextInt(items.size()));
            int amount = 1 + rand.nextInt(16);
            inv.setItem(4, new ItemStack(mat, amount));
        }
    }

}