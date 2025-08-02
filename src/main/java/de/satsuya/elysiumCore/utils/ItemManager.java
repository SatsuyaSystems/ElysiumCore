package de.satsuya.elysiumCore.utils;

import de.satsuya.elysiumCore.classes.BaseItemResult;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ItemManager {
    public static ItemStack getLootboxItem() {
        BaseItemResult lootboxItem = getBaseItem(Material.CHEST, "§6Lootbox", List.of("§7Rechtsklick zum Öffnen"));
        ItemStack lootboxItemStack = lootboxItem.getItemStack();

        ItemMeta lootboxMeta = lootboxItemStack.getItemMeta();
        PersistentDataContainer lootboxData = lootboxItem.getDataContainer();

        lootboxData.set(NamespaceKeys.LOOT_BOX.getKey(), PersistentDataType.BOOLEAN, Boolean.TRUE);
        lootboxItemStack.setItemMeta(lootboxMeta);
        return lootboxItemStack;
    }

    public static ItemStack getTeleporterItem() {
        BaseItemResult result = getBaseItem(Material.ENDER_PEARL, "§6Teleporter", List.of("§7Werfen um zum Layer Spawn zu teleportieren"));
        result.getDataContainer().set(NamespaceKeys.TELEPORTER.getKey(), PersistentDataType.BOOLEAN, Boolean.TRUE);
        ItemStack item = result.getItemStack();
        item.setItemMeta(item.getItemMeta());
        return item;
    }

    public static ItemStack getHealCrystal() {
        BaseItemResult result = getBaseItem(Material.AMETHYST_SHARD, "§6Heal Crystal", List.of("§7Rechtsklick zum Verwenden", "§7Stellt 20 HP wieder her"));
        result.getDataContainer().set(NamespaceKeys.HEAL_CRYSTAL.getKey(), PersistentDataType.BOOLEAN, Boolean.TRUE);
        ItemStack item = result.getItemStack();
        item.setItemMeta(item.getItemMeta());
        return item;
    }

    public static ItemStack getTestItem() {
        BaseItemResult result = getBaseItem(Material.CROSSBOW, "§6Test Item", List.of("§7Dies ist ein Test Item"));
        ItemMeta testMeta = result.getItemStack().getItemMeta();
        testMeta.setCustomModelData(1000);
        ItemStack item = result.getItemStack();
        item.setItemMeta(testMeta);
        return item;
    }

    private static BaseItemResult getBaseItem(Material item, String name, List<String> lore) {
        ItemStack newItem = new ItemStack(item);
        ItemMeta newItemMeta = newItem.getItemMeta();
        PersistentDataContainer itemContainer = newItemMeta.getPersistentDataContainer();
        newItemMeta.setDisplayName(name);
        newItemMeta.setLore(lore);
        newItem.setItemMeta(newItemMeta);
        return new BaseItemResult(newItem, itemContainer);
    }
}
