package de.satsuya.elysiumCore.utils;

import de.satsuya.elysiumCore.ElysiumCore;
import org.bukkit.NamespacedKey;

public enum NamespaceKeys {
    LOOT_BOX("lootbox"),
    TELEPORTER("teleporter"),
    HEAL_CRYSTAL("healcrystal");

    private final NamespacedKey key;

    NamespaceKeys(String name) {
        this.key = new NamespacedKey(ElysiumCore.getInstance(), name);
    }

    public NamespacedKey getKey() {
        return key;
    }
}