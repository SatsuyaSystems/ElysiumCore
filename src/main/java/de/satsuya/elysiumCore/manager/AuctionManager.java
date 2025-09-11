package de.satsuya.elysiumCore.manager;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import de.satsuya.elysiumCore.interfaces.ManagerInterface;
import de.satsuya.elysiumCore.utils.ElysiumLogger;
import de.satsuya.elysiumCore.utils.ManagerRegistry;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AuctionManager manages auction house listings:
 * - Saving/loading from MongoDB
 * - Creating/removing listings
 * - Purchase processing, including economy transfer
 *
 * Items are saved via Bukkit's standard serialize/deserialize.
 */
@ManagerInterface(name = "auction")
public class AuctionManager {

    private final MongoCollection<Document> collection;
    private final EcoManager ecoManager;

    public AuctionManager() {
        MongoDBManager mongoDBManager = ManagerRegistry.get("mongodb");
        MongoDatabase db = mongoDBManager.getDatabase();
        this.collection = db.getCollection("auction_listings");
        this.ecoManager = ManagerRegistry.get("eco");
    }

    public static class AuctionListing {
        public UUID listingId;
        public UUID sellerUuid;
        public String sellerName;
        public int price;
        public long createdAt;
        public ItemStack item;

        public AuctionListing(UUID listingId, UUID sellerUuid, String sellerName, int price, long createdAt, ItemStack item) {
            this.listingId = listingId;
            this.sellerUuid = sellerUuid;
            this.sellerName = sellerName;
            this.price = price;
            this.createdAt = createdAt;
            this.item = item;
        }
    }

    private Document itemToDocument(ItemStack item) {
        Map<String, Object> map = item == null ? new ItemStack(org.bukkit.Material.AIR).serialize() : item.serialize();
        return new Document(map);
    }

    private ItemStack documentToItem(Document doc) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = new HashMap<>(doc);
        return ItemStack.deserialize(map);
    }

    private Document listingToDoc(AuctionListing l) {
        return new Document("listingId", l.listingId.toString())
                .append("sellerUuid", l.sellerUuid.toString())
                .append("sellerName", l.sellerName)
                .append("price", l.price)
                .append("createdAt", l.createdAt)
                .append("item", itemToDocument(l.item));
    }

    private AuctionListing docToListing(Document d) {
        UUID listingId = UUID.fromString(d.getString("listingId"));
        UUID sellerUuid = UUID.fromString(d.getString("sellerUuid"));
        String sellerName = d.getString("sellerName");
        int price = d.getInteger("price");
        long createdAt = d.getLong("createdAt");
        ItemStack item = documentToItem((Document) d.get("item"));
        return new AuctionListing(listingId, sellerUuid, sellerName, price, createdAt, item);
    }

    public List<AuctionListing> getActiveListings(int page, int pageSize) {
        List<AuctionListing> all = new ArrayList<>();
        for (Document d : collection.find()) {
            try {
                all.add(docToListing(d));
            } catch (Exception ex) {
                ElysiumLogger.error("Error loading listing: " + ex.getMessage());
            }
        }
        // Sort newest first
        all.sort(Comparator.comparingLong((AuctionListing l) -> l.createdAt).reversed());
        if (pageSize <= 0) pageSize = 45;
        if (page < 0) page = 0;
        int from = Math.min(page * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        return all.subList(from, to);
    }

    public AuctionListing createListing(Player seller, ItemStack item, int price) {
        if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
            throw new IllegalArgumentException("Invalid item to list.");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be > 0.");
        }
        AuctionListing listing = new AuctionListing(
                UUID.randomUUID(),
                seller.getUniqueId(),
                seller.getName(),
                price,
                System.currentTimeMillis(),
                item.clone()
        );
        Document doc = listingToDoc(listing);
        collection.replaceOne(Filters.eq("listingId", listing.listingId.toString()), doc, new ReplaceOptions().upsert(true));
        return listing;
    }

    public boolean removeListing(UUID listingId) {
        return collection.deleteOne(Filters.eq("listingId", listingId.toString())).getDeletedCount() > 0;
    }

    public AuctionListing getListing(UUID listingId) {
        Document d = collection.find(Filters.eq("listingId", listingId.toString())).first();
        return d == null ? null : docToListing(d);
    }

    /**
     * Processes the purchase: checks balance, transfers coins, gives item to buyer, and removes listing.
     */
    public boolean purchaseListing(Player buyer, UUID listingId) {
        AuctionListing listing = getListing(listingId);
        if (listing == null) return false;

        // Check if seller is online (optional for name resolution)
        Player sellerPlayer = Bukkit.getPlayer(listing.sellerUuid);
        // Balance check
        if (!ecoManager.hasValidBalance(buyer.getUniqueId(), listing.price)) {
            return false;
        }

        try {
            // Transfer
            if (sellerPlayer != null && sellerPlayer.isOnline()) {
                sellerPlayer.sendMessage(ChatColor.GREEN + "Dein Item: " + listing.item.getType()+ " wurde für " + listing.price + "COL gekauft!");
            }
            ecoManager.transfere(buyer.getUniqueId(), listing.sellerUuid, listing.price);

            // Give item to buyer
            HashMap<Integer, ItemStack> leftovers = buyer.getInventory().addItem(listing.item.clone());
            if (!leftovers.isEmpty()) {
                // If inventory is full, drop at player
                leftovers.values().forEach(it -> buyer.getWorld().dropItemNaturally(buyer.getLocation(), it));
            }

            // Remove listing
            removeListing(listingId);
            return true;
        } catch (Exception ex) {
            ElysiumLogger.error("Error during purchase process: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Helper function for placeholder/pagination calculation.
     */
    public int countListings() {
        return (int) collection.countDocuments();
    }

    /**
     * Optional: Player's own listings (e.g., to allow cancellation).
     */
    public List<AuctionListing> getListingsBySeller(UUID sellerUuid) {
        List<AuctionListing> list = new ArrayList<>();
        for (Document d : collection.find(Filters.eq("sellerUuid", sellerUuid.toString()))) {
            list.add(docToListing(d));
        }
        return list.stream()
                .sorted(Comparator.comparingLong((AuctionListing l) -> l.createdAt).reversed())
                .collect(Collectors.toList());
    }
}