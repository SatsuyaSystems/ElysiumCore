package de.satsuya.elysiumCore.manager;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import de.satsuya.elysiumCore.interfaces.ManagerInterface;
import de.satsuya.elysiumCore.utils.ElysiumLogger;
import de.satsuya.elysiumCore.utils.ManagerRegistry;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import java.util.HashMap;
import java.util.Map;

@ManagerInterface(name = "inventory")
public class InventoryManager {
    private final MongoCollection<Document> inventoryCollection;

    public InventoryManager() {
        MongoDBManager mongoDBManager = ManagerRegistry.get("mongodb");
        MongoDatabase database = mongoDBManager.getDatabase();
        this.inventoryCollection = database.getCollection("player_inventories");
    }

    private ItemStack documentToItem(Document doc) {
        if (doc == null || doc.isEmpty() || "AIR".equals(doc.getString("type"))) {
            return new ItemStack(Material.AIR);
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = new HashMap<>(doc);
            return ItemStack.deserialize(map);
        } catch (IllegalArgumentException e) {
            ElysiumLogger.error("Failed to deserialize ItemStack from map. Error: " + e.getMessage() + ". Document: " + doc.toJson());
            return new ItemStack(Material.AIR);
        }
    }

    private Document itemToDocument(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return new Document("type", "AIR");
        }
        Map<String, Object> map = item.serialize();
        if (map == null) {
            ElysiumLogger.error("ItemStack.serialize() returned null for item: " + item.getType().toString());
            return new Document("type", "AIR");
        }
        return new Document(map);
    }

    /**
     * Saves a player's inventory to the database, with all slots nested in an 'inventory' key.
     *
     * @param player The player to save.
     */
    public void saveInventory(Player player) {
        try {
            ElysiumLogger.debug("Attempting to save inventory for player: " + player.getName() + " with UUID: " + player.getUniqueId().toString());
            PlayerInventory inv = player.getInventory();
            Document mainDoc = new Document("uuid", player.getUniqueId().toString())
                    .append("name", player.getDisplayName());

            Document inventoryDoc = new Document();

            // Save main inventory and hotbar slots
            ElysiumLogger.debug("Serializing main inventory and hotbar slots...");
            for (int i = 0; i < inv.getSize(); i++) {
                inventoryDoc.append("slot_" + i, itemToDocument(inv.getItem(i)));
            }

            // Save armor contents
            ElysiumLogger.debug("Serializing armor contents...");
            inventoryDoc.append("armor_head", itemToDocument(inv.getHelmet()));
            inventoryDoc.append("armor_chest", itemToDocument(inv.getChestplate()));
            inventoryDoc.append("armor_legs", itemToDocument(inv.getLeggings()));
            inventoryDoc.append("armor_feet", itemToDocument(inv.getBoots()));

            // Save off-hand item
            ElysiumLogger.debug("Serializing off-hand item...");
            inventoryDoc.append("offhand", itemToDocument(inv.getItemInOffHand()));

            mainDoc.append("inventory", inventoryDoc);

            // Replace the existing document or insert a new one if it doesn't exist
            inventoryCollection.replaceOne(
                    new Document("uuid", player.getUniqueId().toString()),
                    mainDoc,
                    new ReplaceOptions().upsert(true)
            );
            ElysiumLogger.log("Saved inventory for " + player.getName());
        } catch (Exception e) {
            ElysiumLogger.error("Failed to save inventory for " + player.getName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads a player's inventory from the database, reading from the nested 'inventory' key.
     *
     * @param player The player to load.
     */
    public void loadInventory(Player player) {
        try {
            ElysiumLogger.debug("Attempting to load inventory for player: " + player.getName() + " with UUID: " + player.getUniqueId().toString());
            Document doc = inventoryCollection.find(new Document("uuid", player.getUniqueId().toString())).first();

            if (doc != null && doc.containsKey("inventory")) {
                ElysiumLogger.debug("Found inventory document for player.");
                PlayerInventory inv = player.getInventory();
                inv.clear();

                Document inventoryDoc = doc.get("inventory", Document.class);

                // Load main inventory and hotbar slots
                ElysiumLogger.debug("Deserializing main inventory slots...");
                for (int i = 0; i < inv.getSize(); i++) {
                    String slotKey = "slot_" + i;
                    if (inventoryDoc.containsKey(slotKey)) {
                        Document itemDoc = inventoryDoc.get(slotKey, Document.class);
                        inv.setItem(i, documentToItem(itemDoc));
                    }
                }

                // Load armor contents
                ElysiumLogger.debug("Deserializing armor contents...");
                if (inventoryDoc.containsKey("armor_head")) {
                    inv.setHelmet(documentToItem(inventoryDoc.get("armor_head", Document.class)));
                }
                if (inventoryDoc.containsKey("armor_chest")) {
                    inv.setChestplate(documentToItem(inventoryDoc.get("armor_chest", Document.class)));
                }
                if (inventoryDoc.containsKey("armor_legs")) {
                    inv.setLeggings(documentToItem(inventoryDoc.get("armor_legs", Document.class)));
                }
                if (inventoryDoc.containsKey("armor_feet")) {
                    inv.setBoots(documentToItem(inventoryDoc.get("armor_feet", Document.class)));
                }

                // Load off-hand item
                ElysiumLogger.debug("Deserializing off-hand item...");
                if (inventoryDoc.containsKey("offhand")) {
                    inv.setItemInOffHand(documentToItem(inventoryDoc.get("offhand", Document.class)));
                }

                ElysiumLogger.log("Loaded inventory for " + player.getName());
            } else {
                ElysiumLogger.log("No inventory found for " + player.getName() + " or inventory key is missing.");
            }
        } catch (Exception e) {
            ElysiumLogger.error("Failed to load inventory for " + player.getName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
}