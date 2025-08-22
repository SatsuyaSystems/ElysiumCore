// MongoDBManager.java
package de.satsuya.elysiumCore.utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import de.satsuya.elysiumCore.ElysiumCore;
import dev.lone.itemsadder.api.CustomStack;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MongoDBManager {

    private MongoClient mongoClient;
    private MongoDatabase database;

    public MongoDBManager(String uri, String dbName) {
        try {
            mongoClient = MongoClients.create(uri);
            database = mongoClient.getDatabase(dbName);
        } catch (Exception e) {
            ElysiumLogger.error("Could not connect to MongoDB!");
        }
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    /**
     * Converts an ItemStack to a JSONObject, including name, enchantments, and NBT data.
     * This version dynamically serializes all keys from the PersistentDataContainer.
     * @param item The ItemStack to convert.
     * @return A JSONObject representing the item.
     */
    private JSONObject itemStackToJson(ItemStack item) {
        JSONObject jsonItem = new JSONObject();
        if (item == null || item.getType() == Material.AIR) {
            jsonItem.put("type", "AIR");
            jsonItem.put("amount", 0L);
        } else {
            // Check if the item is a custom ItemsAdder item
            CustomStack customStack = CustomStack.byItemStack(item);
            if (customStack != null) {
                // If it's a custom item, save its ItemsAdder ID
                jsonItem.put("itemsadder_id", customStack.getNamespace() + ":" + customStack.getId());
                // The actual Bukkit type is also useful for a fallback
                jsonItem.put("type", item.getType().toString());
            } else {
                // It's a regular Minecraft item
                jsonItem.put("type", item.getType().toString());
            }

            jsonItem.put("amount", (long) item.getAmount());

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                // Serialize item durability
                if (meta instanceof Damageable) {
                    Damageable damageableMeta = (Damageable) meta;
                    jsonItem.put("durability", (long) damageableMeta.getDamage());
                }

                // Serialize book data
                if (meta instanceof BookMeta) {
                    BookMeta bookMeta = (BookMeta) meta;
                    if (bookMeta.hasTitle()) {
                        jsonItem.put("bookTitle", bookMeta.getTitle());
                    }
                    if (bookMeta.hasAuthor()) {
                        jsonItem.put("bookAuthor", bookMeta.getAuthor());
                    }
                    if (bookMeta.hasPages()) {
                        JSONArray pagesArray = new JSONArray();
                        for (String page : bookMeta.getPages()) {
                            pagesArray.add(page);
                        }
                        jsonItem.put("bookPages", pagesArray);
                    }
                }

                // Serialize components (lore, custom name, and custom data)
                JSONObject componentsJson = new JSONObject();

                // Serialize custom name
                if (meta.hasDisplayName()) {
                    JSONObject nameObject = new JSONObject();
                    nameObject.put("text", meta.getDisplayName());
                    JSONArray extraArray = new JSONArray();
                    extraArray.add(nameObject);
                    JSONObject nameComponent = new JSONObject();
                    nameComponent.put("extra", extraArray);
                    componentsJson.put("minecraft:custom_name", nameComponent.toJSONString());
                }

                // Serialize lore
                if (meta.hasLore()) {
                    JSONArray loreArray = new JSONArray();
                    for (String loreLine : meta.getLore()) {
                        JSONObject loreObject = new JSONObject();
                        loreObject.put("text", loreLine);
                        loreArray.add(loreObject);
                    }
                    componentsJson.put("minecraft:lore", loreArray.toJSONString());
                }

                // Serialize PersistentDataContainer into "minecraft:custom_data"
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                if (!pdc.getKeys().isEmpty()) {
                    JSONObject customDataComponent = new JSONObject();
                    JSONObject publicBukkitValues = new JSONObject();

                    for (NamespacedKey key : pdc.getKeys()) {
                        Object value = null;
                        if (pdc.has(key, PersistentDataType.BYTE)) {
                            value = pdc.get(key, PersistentDataType.BYTE);
                        } else if (pdc.has(key, PersistentDataType.SHORT)) {
                            value = pdc.get(key, PersistentDataType.SHORT);
                        } else if (pdc.has(key, PersistentDataType.INTEGER)) {
                            value = pdc.get(key, PersistentDataType.INTEGER);
                        } else if (pdc.has(key, PersistentDataType.LONG)) {
                            value = pdc.get(key, PersistentDataType.LONG);
                        } else if (pdc.has(key, PersistentDataType.FLOAT)) {
                            value = pdc.get(key, PersistentDataType.FLOAT);
                        } else if (pdc.has(key, PersistentDataType.DOUBLE)) {
                            value = pdc.get(key, PersistentDataType.DOUBLE);
                        } else if (pdc.has(key, PersistentDataType.STRING)) {
                            value = pdc.get(key, PersistentDataType.STRING);
                        } else if (pdc.has(key, PersistentDataType.BOOLEAN)) {
                            value = pdc.get(key, PersistentDataType.BOOLEAN);
                        } else if (pdc.has(key, PersistentDataType.BYTE_ARRAY)) {
                            byte[] byteArray = pdc.get(key, PersistentDataType.BYTE_ARRAY);
                            JSONArray jsonArray = new JSONArray();
                            for (byte b : byteArray) {
                                jsonArray.add((long) b);
                            }
                            value = jsonArray;
                        } else if (pdc.has(key, PersistentDataType.INTEGER_ARRAY)) {
                            int[] intArray = pdc.get(key, PersistentDataType.INTEGER_ARRAY);
                            JSONArray jsonArray = new JSONArray();
                            for (int i : intArray) {
                                jsonArray.add((long) i);
                            }
                            value = jsonArray;
                        }

                        if (value != null) {
                            publicBukkitValues.put(key.toString(), value);
                        }
                    }
                    if (!publicBukkitValues.isEmpty()) {
                        customDataComponent.put("PublicBukkitValues", publicBukkitValues);
                        componentsJson.put("minecraft:custom_data", customDataComponent.toJSONString());
                    }
                }

                if (!componentsJson.isEmpty()) {
                    jsonItem.put("components", componentsJson);
                }

                // Serialize enchantments
                if (meta.hasEnchants()) {
                    JSONObject enchantments = new JSONObject();
                    for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                        enchantments.put(entry.getKey().getKey().toString(), (long) entry.getValue());
                    }
                    jsonItem.put("enchantments", enchantments);
                }
            }
        }
        return jsonItem;
    }

    /**
     * Converts a JSONObject back to an ItemStack by manually constructing it.
     * @param jsonItem The JSONObject to convert.
     * @return An ItemStack.
     */
    private ItemStack jsonToItemStack(JSONObject jsonItem) {
        Object typeObj = jsonItem.get("type");
        if (typeObj == null || !(typeObj instanceof String)) {
            ElysiumLogger.error("Failed to deserialize item: JSON object is missing a valid 'type' key.");
            return new ItemStack(Material.AIR);
        }

        String typeString = (String) typeObj;
        if ("AIR".equals(typeString)) {
            return new ItemStack(Material.AIR, 0);
        }

        // Try to load ItemsAdder item first
        if (jsonItem.containsKey("itemsadder_id")) {
            String customItemId = (String) jsonItem.get("itemsadder_id");
            ElysiumLogger.debug("Attempting to restore ItemsAdder custom item with ID: " + customItemId);

            CustomStack customStack = CustomStack.getInstance(customItemId);
            if (customStack != null) {
                ItemStack customItem = customStack.getItemStack();
                ElysiumLogger.debug("Successfully restored custom item: " + customItemId);

                // Re-apply amount, durability, and enchantments that might have been changed
                if (jsonItem.containsKey("amount")) {
                    customItem.setAmount(((Long) jsonItem.get("amount")).intValue());
                }
                if (jsonItem.containsKey("durability") && customItem.getItemMeta() instanceof Damageable) {
                    Damageable damageableMeta = (Damageable) customItem.getItemMeta();
                    damageableMeta.setDamage(((Long) jsonItem.get("durability")).intValue());
                }
                if (jsonItem.containsKey("enchantments")) {
                    JSONObject enchantmentsJson = (JSONObject) jsonItem.get("enchantments");
                    for (Object keyObj : enchantmentsJson.keySet()) {
                        String keyString = (String) keyObj;
                        try {
                            NamespacedKey key = NamespacedKey.fromString(keyString);
                            if (key != null) {
                                int level = ((Long) enchantmentsJson.get(keyString)).intValue();
                                Enchantment enchantment = Enchantment.getByKey(key);
                                if (enchantment != null) {
                                    customItem.addUnsafeEnchantment(enchantment, level);
                                }
                            }
                        } catch (Exception e) {
                            ElysiumLogger.error("Failed to deserialize enchantment for key: " + keyString + " - " + e.getMessage());
                        }
                    }
                }
                return customItem;
            } else {
                ElysiumLogger.error("ItemsAdder could not find item with ID: " + customItemId + ". Falling back to standard item.");
            }
        }

        // Fallback to standard item creation if no ItemsAdder data is found or if the item wasn't found
        Material material = Material.getMaterial(typeString);
        if (material == null) {
            ElysiumLogger.error("Failed to deserialize item: Material " + typeString + " is not a valid Bukkit material.");
            return new ItemStack(Material.AIR);
        }

        int amount = 1;
        if (jsonItem.containsKey("count")) {
            amount = ((Long) jsonItem.get("count")).intValue();
        } else if (jsonItem.containsKey("amount")) {
            amount = ((Long) jsonItem.get("amount")).intValue();
        }

        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Set item durability
            if (jsonItem.containsKey("durability") && meta instanceof Damageable) {
                Damageable damageableMeta = (Damageable) meta;
                damageableMeta.setDamage(((Long) jsonItem.get("durability")).intValue());
            }

            // Set book data
            if (meta instanceof BookMeta) {
                BookMeta bookMeta = (BookMeta) meta;
                if (jsonItem.containsKey("bookTitle")) {
                    bookMeta.setTitle((String) jsonItem.get("bookTitle"));
                }
                if (jsonItem.containsKey("bookAuthor")) {
                    bookMeta.setAuthor((String) jsonItem.get("bookAuthor"));
                }
                if (jsonItem.containsKey("bookPages")) {
                    JSONArray pagesJson = (JSONArray) jsonItem.get("bookPages");
                    if (pagesJson != null) {
                        List<String> pagesList = new ArrayList<>();
                        for (Object pageObj : pagesJson) {
                            pagesList.add(String.valueOf(pageObj));
                        }
                        bookMeta.setPages(pagesList);
                    }
                }
            }

            // Set enchantments
            if (jsonItem.containsKey("enchantments")) {
                JSONObject enchantmentsJson = (JSONObject) jsonItem.get("enchantments");
                for (Object keyObj : enchantmentsJson.keySet()) {
                    String keyString = (String) keyObj;
                    try {
                        NamespacedKey key = NamespacedKey.fromString(keyString);
                        if (key != null) {
                            int level = ((Long) enchantmentsJson.get(keyString)).intValue();
                            Enchantment enchantment = Enchantment.getByKey(key);
                            if (enchantment != null) {
                                meta.addEnchant(enchantment, level, true);
                            }
                        }
                    } catch (Exception e) {
                        ElysiumLogger.error("Failed to deserialize enchantment for key: " + keyString + " - " + e.getMessage());
                    }
                }
            }

            // Handle the newer "components" key for name and lore
            if (jsonItem.containsKey("components")) {
                JSONObject componentsJson = (JSONObject) jsonItem.get("components");
                JSONParser parser = new JSONParser();

                // Set custom name
                if (componentsJson.containsKey("minecraft:custom_name")) {
                    try {
                        String customNameJsonString = (String) componentsJson.get("minecraft:custom_name");
                        Object parsedName = parser.parse(customNameJsonString);

                        // Handle both single text object and 'extra' array format
                        if (parsedName instanceof JSONObject) {
                            JSONObject customNameObject = (JSONObject) parsedName;
                            if (customNameObject.containsKey("text")) {
                                meta.setDisplayName((String) customNameObject.get("text"));
                            } else if (customNameObject.containsKey("extra")) {
                                JSONArray extra = (JSONArray) customNameObject.get("extra");
                                if (extra != null && !extra.isEmpty()) {
                                    JSONObject extraObject = (JSONObject) extra.get(0);
                                    String customName = (String) extraObject.get("text");
                                    meta.setDisplayName(customName);
                                }
                            }
                        } else if (parsedName instanceof JSONArray) {
                            // Some formats might be just a plain JSON array
                            JSONArray extra = (JSONArray) parsedName;
                            if (extra != null && !extra.isEmpty()) {
                                JSONObject extraObject = (JSONObject) extra.get(0);
                                String customName = (String) extraObject.get("text");
                                meta.setDisplayName(customName);
                            }
                        }
                    } catch (Exception e) {
                        ElysiumLogger.error("Failed to parse custom name component from JSON: " + e.getMessage());
                    }
                }

                // Set lore
                if (componentsJson.containsKey("minecraft:lore")) {
                    try {
                        String loreJsonString = (String) componentsJson.get("minecraft:lore");
                        JSONArray loreArray = (JSONArray) parser.parse(loreJsonString);
                        List<String> loreList = new ArrayList<>();
                        for (Object obj : loreArray) {
                            JSONObject loreObject = (JSONObject) obj;
                            if (loreObject.containsKey("text")) {
                                loreList.add((String) loreObject.get("text"));
                            } else {
                                JSONArray extra = (JSONArray) loreObject.get("extra");
                                if (extra != null && !extra.isEmpty()) {
                                    JSONObject extraObject = (JSONObject) extra.get(0);
                                    String loreText = (String) extraObject.get("text");
                                    loreList.add(loreText);
                                }
                            }
                        }
                        meta.setLore(loreList);
                    } catch (ParseException e) {
                        ElysiumLogger.error("Failed to parse lore from JSON: " + e.getMessage());
                    }
                }

                // Manually apply PersistentDataContainer from the nested components
                if (componentsJson.containsKey("minecraft:custom_data")) {
                    try {
                        String customDataString = (String) componentsJson.get("minecraft:custom_data");
                        JSONObject customDataObject = (JSONObject) parser.parse(customDataString);
                        JSONObject publicBukkitValues = (JSONObject) customDataObject.get("PublicBukkitValues");
                        PersistentDataContainer pdc = meta.getPersistentDataContainer();
                        ElysiumCore plugin = ElysiumCore.getInstance();

                        for (Object keyObj : publicBukkitValues.keySet()) {
                            String keyString = (String) keyObj;
                            Object value = publicBukkitValues.get(keyString);
                            try {
                                String[] parts = keyString.split(":");
                                if (parts.length != 2) continue;
                                NamespacedKey key = new NamespacedKey(plugin, parts[1]);

                                // Check for the value type first and then set the PersistentDataType.
                                if (value instanceof Boolean) {
                                    pdc.set(key, PersistentDataType.BOOLEAN, (Boolean) value);
                                } else if (value instanceof Long) {
                                    long longValue = ((Long) value).longValue();
                                    // A common pattern is for booleans to be serialized as 1 or 0.
                                    if (longValue == 0 || longValue == 1) {
                                        pdc.set(key, PersistentDataType.BOOLEAN, longValue == 1);
                                    } else if (longValue <= Integer.MAX_VALUE && longValue >= Integer.MIN_VALUE) {
                                        pdc.set(key, PersistentDataType.INTEGER, (int) longValue);
                                    } else {
                                        pdc.set(key, PersistentDataType.LONG, longValue);
                                    }
                                } else if (value instanceof String) {
                                    pdc.set(key, PersistentDataType.STRING, (String) value);
                                    // Special handling for ItemsAdder custom items
                                    if (key.getKey().equals("custom_item") && (key.getNamespace().equals("itemsadder") || key.getNamespace().equals("_elysium"))) {
                                        ElysiumLogger.debug("Restored ItemsAdder custom item with ID: " + (String) value);
                                    }
                                } else if (value instanceof Double) {
                                    pdc.set(key, PersistentDataType.DOUBLE, (Double) value);
                                } else if (value instanceof JSONArray) {
                                    JSONArray jsonArray = (JSONArray) value;
                                    if (!jsonArray.isEmpty()) {
                                        Object firstElement = jsonArray.get(0);
                                        if (firstElement instanceof Long) {
                                            int[] intArray = new int[jsonArray.size()];
                                            boolean isIntArray = true;
                                            for (int i = 0; i < jsonArray.size(); i++) {
                                                if (jsonArray.get(i) instanceof Long) {
                                                    long elementValue = ((Long) jsonArray.get(i)).longValue();
                                                    if (elementValue <= Integer.MAX_VALUE && elementValue >= Integer.MIN_VALUE) {
                                                        intArray[i] = (int) elementValue;
                                                    } else {
                                                        isIntArray = false;
                                                        break;
                                                    }
                                                } else {
                                                    isIntArray = false;
                                                    break;
                                                }
                                            }
                                            if (isIntArray) {
                                                pdc.set(key, PersistentDataType.INTEGER_ARRAY, intArray);
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                ElysiumLogger.error("Failed to deserialize persistent data for key: " + keyString + " - " + e.getMessage());
                            }
                        }
                    } catch (ParseException e) {
                        ElysiumLogger.error("Failed to parse custom data from JSON: " + e.getMessage());
                    }
                }
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Saves a player's inventory to the database as a JSON string.
     * @param player The player to save.
     */
    public void saveInventory(Player player) {
        try {
            ElysiumLogger.debug("Attempting to save inventory for player: " + player.getName() + " with UUID: " + player.getUniqueId().toString());
            PlayerInventory inv = player.getInventory();
            JSONObject jsonInventory = new JSONObject();

            // Save main inventory and hotbar
            JSONArray mainInventory = new JSONArray();
            ElysiumLogger.debug("Serializing main inventory and hotbar...");
            for (int i = 0; i < inv.getSize(); i++) {
                mainInventory.add(itemStackToJson(inv.getItem(i)));
            }
            jsonInventory.put("main_inventory", mainInventory);

            // Save armor contents
            JSONArray armorContents = new JSONArray();
            ElysiumLogger.debug("Serializing armor contents...");
            for (ItemStack item : inv.getArmorContents()) {
                armorContents.add(itemStackToJson(item));
            }
            jsonInventory.put("armor_contents", armorContents);

            // Save off-hand item
            ElysiumLogger.debug("Serializing off-hand item...");
            jsonInventory.put("offhand_item", itemStackToJson(inv.getItemInOffHand()));

            // Create the BSON Document with the JSON string
            Document doc = new Document("uuid", player.getUniqueId().toString())
                    .append("inventory", jsonInventory.toJSONString());

            ElysiumLogger.debug("Final BSON document being saved: " + doc.toJson());

            database.getCollection("player_inventories").replaceOne(
                    new Document("uuid", player.getUniqueId().toString()),
                    doc,
                    new ReplaceOptions().upsert(true)
            );
            ElysiumLogger.log("Saved inventory for " + player.getName());
        } catch (Exception e) {
            ElysiumLogger.error("Failed to save inventory for " + player.getName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads a player's inventory from the database.
     * @param player The player to load.
     */
    public void loadInventory(Player player) {
        try {
            ElysiumLogger.debug("Attempting to load inventory for player: " + player.getName() + " with UUID: " + player.getUniqueId().toString());
            Document doc = database.getCollection("player_inventories").find(new Document("uuid", player.getUniqueId().toString())).first();

            if (doc != null && doc.containsKey("inventory")) {
                ElysiumLogger.debug("Found inventory document for player.");
                String inventoryJsonString = doc.getString("inventory");
                ElysiumLogger.debug("Raw inventory JSON string: " + inventoryJsonString);
                JSONParser parser = new JSONParser();
                JSONObject jsonInventory = (JSONObject) parser.parse(inventoryJsonString);
                ElysiumLogger.debug("Parsed JSON object: " + jsonInventory.toJSONString());
                PlayerInventory inv = player.getInventory();

                inv.clear();
                ElysiumLogger.debug("Player inventory cleared.");

                // Load main inventory and hotbar
                JSONArray mainInventory = (JSONArray) jsonInventory.get("main_inventory");
                ElysiumLogger.debug("Loading main inventory, size: " + mainInventory.size());
                for (int i = 0; i < mainInventory.size(); i++) {
                    JSONObject itemJson = (JSONObject) mainInventory.get(i);
                    ElysiumLogger.debug("Deserializing item at slot " + i + ": " + itemJson.toJSONString());
                    inv.setItem(i, jsonToItemStack(itemJson));
                }

                // Load armor contents
                JSONArray armorContents = (JSONArray) jsonInventory.get("armor_contents");
                ElysiumLogger.debug("Loading armor contents, size: " + armorContents.size());
                ItemStack[] armor = new ItemStack[4];
                for (int i = 0; i < armorContents.size(); i++) {
                    JSONObject itemJson = (JSONObject) armorContents.get(i);
                    ElysiumLogger.debug("Deserializing armor piece at slot " + i + ": " + itemJson.toJSONString());
                    armor[i] = jsonToItemStack(itemJson);
                }
                inv.setArmorContents(armor);

                // Load off-hand item
                JSONObject offhandJson = (JSONObject) jsonInventory.get("offhand_item");
                ElysiumLogger.debug("Loading off-hand item: " + offhandJson.toJSONString());
                inv.setItemInOffHand(jsonToItemStack(offhandJson));

                ElysiumLogger.log("Loaded inventory for " + player.getName());
            } else {
                ElysiumLogger.log("No inventory found for " + player.getName());
            }
        } catch (Exception e) {
            ElysiumLogger.error("Failed to load inventory for " + player.getName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
}
