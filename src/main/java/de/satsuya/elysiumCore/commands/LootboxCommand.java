package de.satsuya.elysiumCore.commands;

import de.satsuya.elysiumCore.utils.NamespaceKeys;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;


public class LootboxCommand implements PluginCommand{
    @Override
    public String getName() {
        return "lootbox"; // The name of the command
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(NamespaceKeys.LootBox, PersistentDataType.BOOLEAN, Boolean.TRUE);
        meta.setDisplayName("§6Lootbox");
        meta.setLore(java.util.List.of("§7Rechtsklick zum Öffnen"));
        meta.setItemName("§6Lootbox");
        item.setItemMeta(meta);
        Player player = (Player) sender;
        player.getInventory().addItem(item);
        player.sendMessage("§aDu hast eine Lootbox erhalten! §7(§6Rechtsklick zum Öffnen§7)");
        return true;
    }
}
