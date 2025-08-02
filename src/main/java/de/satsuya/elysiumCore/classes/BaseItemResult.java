package de.satsuya.elysiumCore.classes;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

public class BaseItemResult {
    private final ItemStack itemStack;
    private final PersistentDataContainer dataContainer;

    public BaseItemResult(ItemStack itemStack, PersistentDataContainer dataContainer) {
        this.itemStack = itemStack;
        this.dataContainer = dataContainer;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public PersistentDataContainer getDataContainer() {
        return dataContainer;
    }
}

