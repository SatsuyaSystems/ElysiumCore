package de.satsuya.elysiumCore.commands;

import de.satsuya.elysiumCore.interfaces.PluginCommand;
import de.satsuya.elysiumCore.manager.AuctionManager;
import de.satsuya.elysiumCore.utils.ManagerRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@PluginCommand(name = "ah")
public class AuctionCommand implements CommandExecutor, TabExecutor {

    public static final String AUCTION_TITLE = "Auktionshaus";
    public static final String LISTING_TITLE = "Auktionshaus - Liste";

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        openAuctionMain(player, 0);
        return true;
    }

    public static void openAuctionMain(Player player, int page) {
        AuctionManager auctionManager = ManagerRegistry.get("auction");
        Inventory inv = Bukkit.createInventory(null, 54, AUCTION_TITLE);

        // Lade Listings (max 45 Slots für Items)
        List<AuctionManager.AuctionListing> listings = auctionManager.getActiveListings(page, 45);
        for (int i = 0; i < listings.size() && i < 45; i++) {
            AuctionManager.AuctionListing l = listings.get(i);
            ItemStack it = l.item.clone();
            ItemMeta meta = it.getItemMeta();
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add("§7Preis: §a" + l.price + " COL");
            lore.add("§7Verkäufer: §b" + l.sellerName);
            lore.add("§8Listing-ID: " + l.listingId.toString());
            meta.setLore(lore);
            it.setItemMeta(meta);
            inv.setItem(i, it);
        }

        // Untere Leiste: Listen-Button und Navigation
        ItemStack listButton = new ItemStack(Material.EMERALD);
        ItemMeta lm = listButton.getItemMeta();
        lm.setDisplayName("§aItem listen");
        listButton.setItemMeta(lm);
        inv.setItem(49, listButton);

        // Simple Navigation
        ItemStack prev = new ItemStack(Material.ARROW);
        ItemMeta pm = prev.getItemMeta();
        pm.setDisplayName("§eZurück");
        prev.setItemMeta(pm);
        inv.setItem(45, prev);

        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta nm = next.getItemMeta();
        nm.setDisplayName("§eWeiter");
        next.setItemMeta(nm);
        inv.setItem(53, next);

        player.openInventory(inv);
    }

    public static void openListingGui(Player player, ItemStack presetItem, int startPrice) {
        Inventory inv = Bukkit.createInventory(null, 54, LISTING_TITLE);
        // Slot 13 ist der Item-Slot
        if (presetItem != null) {
            inv.setItem(13, presetItem);
        }

        // Preissteuerung unten
        // Slots: 45:-100, 46:-10, 47:-1, 49:Bestätigen, 51:+1, 52:+10, 53:+100
        inv.setItem(45, makeButton(Material.REDSTONE_BLOCK, "§c-100"));
        inv.setItem(46, makeButton(Material.REDSTONE, "§c-10"));
        inv.setItem(47, makeButton(Material.BARRIER, "§c-1"));
        inv.setItem(49, makeButton(Material.LIME_WOOL, "§aBestätigen"));
        inv.setItem(51, makeButton(Material.GREEN_DYE, "§a+1"));
        inv.setItem(52, makeButton(Material.EMERALD, "§a+10"));
        inv.setItem(53, makeButton(Material.EMERALD_BLOCK, "§a+100"));

        // Preisanzeige in Slot 22
        ItemStack price = new ItemStack(Material.PAPER);
        ItemMeta pm = price.getItemMeta();
        pm.setDisplayName("§ePreis: §a" + Math.max(0, startPrice) + " COL");
        price.setItemMeta(pm);
        inv.setItem(22, price);

        // Hinweis-Item
        inv.setItem(4, hint("§7Platziere dein Item §funter diesem Slot§7 und stelle den Preis unten ein."));

        player.openInventory(inv);
    }

    private static ItemStack makeButton(Material mat, String name) {
        ItemStack it = new ItemStack(mat);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(name);
        it.setItemMeta(m);
        return it;
    }

    private static ItemStack hint(String name) {
        ItemStack it = new ItemStack(Material.BOOK);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(name);
        m.setLore(List.of("Du kannst dein Item nur durch Selbstkauf zurück bekommen!","Wenn du das GUI mit Item drinne verlässt buggt es weg :3 also pass auf!"));
        it.setItemMeta(m);
        return it;
    }

    public static int extractPrice(Inventory inv) {
        ItemStack price = inv.getItem(22);
        if (price == null || !price.hasItemMeta() || price.getItemMeta().getDisplayName() == null) return 0;
        String dn = price.getItemMeta().getDisplayName().replace("§ePreis: §a", "").replace(" COL", "").trim();
        try {
            return Integer.parseInt(dn);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static void setPrice(Inventory inv, int newPrice) {
        newPrice = Math.max(0, newPrice);
        ItemStack price = inv.getItem(22);
        if (price == null || price.getType() == Material.AIR) {
            price = new ItemStack(Material.PAPER);
        }
        ItemMeta pm = price.getItemMeta();
        pm.setDisplayName("§ePreis: §a" + newPrice + " COL");
        price.setItemMeta(pm);
        inv.setItem(22, price);
    }

    public static UUID extractListingIdFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return null;
        for (String line : item.getItemMeta().getLore()) {
            if (line != null && line.startsWith("§8Listing-ID: ")) {
                String idStr = line.replace("§8Listing-ID: ", "").trim();
                try {
                    return UUID.fromString(idStr);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return null;
    }
}