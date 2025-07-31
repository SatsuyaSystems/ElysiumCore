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
}
