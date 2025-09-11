package de.satsuya.elysiumCore.manager;

import de.satsuya.elysiumCore.interfaces.ManagerInterface;
import de.satsuya.elysiumCore.utils.NamespaceKeys;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

@ManagerInterface(name = "item")
public class ItemManager {
    public static ItemStack getLootboxItem() {
        ItemStack lootboxItemStack = getBaseItem(Material.CHEST, "§6Lootbox", List.of("§7Rechtsklick zum Öffnen"));
        ItemMeta lootboxMeta = lootboxItemStack.getItemMeta();
        PersistentDataContainer lootboxData = lootboxMeta.getPersistentDataContainer();
        lootboxData.set(NamespaceKeys.LOOT_BOX.getKey(), PersistentDataType.BOOLEAN, Boolean.TRUE);
        lootboxItemStack.setItemMeta(lootboxMeta);
        return lootboxItemStack;
    }

    public static ItemStack getTeleporterItem() {
        ItemStack item = getBaseItem(Material.ENDER_PEARL, "§6Teleporter", List.of("§7Werfen um zum Layer Spawn zu teleportieren"));
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(NamespaceKeys.TELEPORTER.getKey(), PersistentDataType.BOOLEAN, Boolean.TRUE);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getHealCrystal() {
        ItemStack item = getBaseItem(Material.AMETHYST_SHARD, "§6Heal Crystal", List.of("§7Rechtsklick zum Verwenden", "§7Stellt 20 HP wieder her"));
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(NamespaceKeys.HEAL_CRYSTAL.getKey(), PersistentDataType.BOOLEAN, Boolean.TRUE);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getTestItem() {
        ItemStack item = getBaseItem(Material.CROSSBOW, "§6Test Item", List.of("§7Dies ist ein Test Item"));
        ItemMeta testMeta = item.getItemMeta();
        testMeta.setCustomModelData(1000);
        item.setItemMeta(testMeta);
        return item;
    }

    private static ItemStack getBaseItem(Material item, String name, List<String> lore) {
        ItemStack newItem = new ItemStack(item);
        ItemMeta newItemMeta = newItem.getItemMeta();
        newItemMeta.setDisplayName(name);
        newItemMeta.setLore(lore);
        newItem.setItemMeta(newItemMeta);
        return newItem;
    }
}
