package de.satsuya.elysiumCore.events;

import de.satsuya.elysiumCore.commands.AuctionCommand;
import de.satsuya.elysiumCore.manager.AuctionManager;
import de.satsuya.elysiumCore.utils.ElysiumLogger;
import de.satsuya.elysiumCore.utils.ManagerRegistry;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AuctionEvent implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getView().getTitle().equals(AuctionCommand.AUCTION_TITLE)) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            int slot = event.getRawSlot();
            // Oberer Bereich: 0-44 sind Listings (45 Slots)
            if (slot >= 0 && slot < 45) {
                // Kaufversuch
                // Listing-ID aus Lore extrahieren
                try {
                    // Wir nutzen die Hilfsfunktion aus AuctionCommand
                    java.util.UUID listingId = AuctionCommand.extractListingIdFromItem(clicked);
                    if (listingId == null) return;
                    AuctionManager auctionManager = ManagerRegistry.get("auction");
                    boolean success = auctionManager.purchaseListing(player, listingId);
                    if (success) {
                        player.sendMessage("§aKauf erfolgreich! Das Item wurde deinem Inventar hinzugefügt.");
                    } else {
                        player.sendMessage("§cKauf fehlgeschlagen. Möglicherweise unzureichendes Guthaben oder Listing nicht mehr verfügbar.");
                    }
                    // Ansicht aktualisieren
                    AuctionCommand.openAuctionMain(player, 0);
                } catch (Exception ex) {
                    ElysiumLogger.error("Purchase error: " + ex.getMessage());
                }
                return;
            }

            // Slot 49: Listing-GUI öffnen
            if (slot == 49) {
                AuctionCommand.openListingGui(player, null, 100); // Default-Startpreis 100
                return;
            }

            // Navigation
            if (slot == 45) {
                AuctionCommand.openAuctionMain(player, 0); // einfache Seite zurück (hier nur Seite 0)
                return;
            }
            if (slot == 53) {
                AuctionCommand.openAuctionMain(player, 0); // einfache Seite vor (hier nur Seite 0)
                return;
            }
        }
        if (event.getView().getTitle().equals(AuctionCommand.LISTING_TITLE)) {
            int raw = event.getRawSlot();
            // Wir erlauben Interaktionen:
            // - Slot 13: Item Platzierung erlaubt (nur im "Top"-Bereich), ansonsten nicht ziehen aus GUI-Slots
            // - Slot 22: Preisanzeige (nicht klickbar)
            // - Preis-Buttons: 45,46,47,49,51,52,53
            // - Alles andere im GUI blockieren
            if (raw < event.getView().getTopInventory().getSize()) {
                // Klick in das GUI
                event.setCancelled(true);

                Inventory top = event.getView().getTopInventory();
                ItemStack current = top.getItem(13);
                int price = AuctionCommand.extractPrice(top);

                if (raw == 13) {
                    // Man darf via Shift-Klick nicht ziehen, daher nur per Cursor tauschen
                    if (event.getClick() == ClickType.LEFT || event.getClick() == ClickType.RIGHT) {
                        ItemStack cursor = event.getCursor();
                        event.setCursor(current);
                        top.setItem(13, cursor);
                    }
                    return;
                }

                switch (raw) {
                    case 45: AuctionCommand.setPrice(top, price - 100); break;
                    case 46: AuctionCommand.setPrice(top, price - 10); break;
                    case 47: AuctionCommand.setPrice(top, Math.max(0, price - 1)); break;
                    case 51: AuctionCommand.setPrice(top, price + 1); break;
                    case 52: AuctionCommand.setPrice(top, price + 10); break;
                    case 53: AuctionCommand.setPrice(top, price + 100); break;
                    case 49:
                        // Bestätigen: Listing erzeugen
                        ItemStack itemToList = top.getItem(13);
                        if (itemToList == null || itemToList.getType() == Material.AIR) {
                            player.sendMessage("§cLege zuerst ein Item in Slot 13.");
                            return;
                        }
                        if (price <= 0) {
                            player.sendMessage("§cPreis muss größer als 0 sein.");
                            return;
                        }
                        try {
                            AuctionManager auctionManager = ManagerRegistry.get("auction");
                            // Item aus GUI nehmen und dem Spieler aus dem Inventar entfernen: Wir nehmen es aus GUI und schließen,
                            // das Item bleibt nicht beim Spieler, da es im GUI liegt.
                            auctionManager.createListing(player, itemToList.clone(), price);
                            top.setItem(13, new ItemStack(Material.AIR));
                            player.sendMessage("§aItem gelistet für §e" + price + " COL§a.");
                            AuctionCommand.openAuctionMain(player, 0);
                        } catch (IllegalArgumentException iae) {
                            player.sendMessage("§c" + iae.getMessage());
                        } catch (Exception ex) {
                            ElysiumLogger.error("Error while listing: " + ex.getMessage());
                            player.sendMessage("§cFehler beim Listen des Items.");
                        }
                        return;
                    default:
                        // nichts
                        break;
                }
            } else {
                // Klick im eigenen Inventar – erlauben: Item in Slot 13 ziehen via normalem Klick
                // Keine Cancel-Änderung hier; Standardverhalten
            }
        }
    }
}