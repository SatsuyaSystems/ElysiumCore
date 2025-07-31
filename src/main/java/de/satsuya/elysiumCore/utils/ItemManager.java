package de.satsuya.elysiumCore.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ItemManager {
    public static ItemStack getLootboxItem() {
        ItemStack lootboxItem = new ItemStack(Material.CHEST);
        ItemMeta lootboxMeta = lootboxItem.getItemMeta();
        PersistentDataContainer lootboxData = lootboxMeta.getPersistentDataContainer();
        lootboxData.set(NamespaceKeys.LootBox, PersistentDataType.BOOLEAN, Boolean.TRUE);
        lootboxMeta.setDisplayName("§6Lootbox");
        lootboxMeta.setLore(List.of("§7Rechtsklick zum Öffnen"));
        lootboxItem.setItemMeta(lootboxMeta);
        return lootboxItem;
    }
    public static ItemStack getTeleporterItem() {
        ItemStack teleporterItem = new ItemStack(Material.ENDER_PEARL);
        ItemMeta teleporterMeta = teleporterItem.getItemMeta();
        PersistentDataContainer teleporterData = teleporterMeta.getPersistentDataContainer();
        teleporterData.set(NamespaceKeys.Teleporter, PersistentDataType.BOOLEAN, Boolean.TRUE);
        teleporterMeta.setDisplayName("§6Teleporter");
        teleporterMeta.setLore(List.of("§7Werfen um zum Layer Spawn zu teleportieren"));
        teleporterItem.setItemMeta(teleporterMeta);
        return teleporterItem;
    }
    public static ItemStack getHealCrystal() {
        ItemStack healCrystalItem = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta healCrystalMeta = healCrystalItem.getItemMeta();
        PersistentDataContainer healCrystalData = healCrystalMeta.getPersistentDataContainer();
        healCrystalData.set(NamespaceKeys.HealCrystal, PersistentDataType.BOOLEAN, Boolean.TRUE);
        healCrystalMeta.setDisplayName("§6Heal Crystal");
        healCrystalMeta.setLore(List.of("§7Rechtsklick zum Verwenden", "§7Stellt 20 HP wieder her"));
        healCrystalItem.setItemMeta(healCrystalMeta);
        return healCrystalItem;
    }
}
