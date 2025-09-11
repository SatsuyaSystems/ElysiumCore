package de.satsuya.elysiumCore.manager;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import de.satsuya.elysiumCore.interfaces.ManagerInterface;
import de.satsuya.elysiumCore.utils.ElysiumLogger;
import de.satsuya.elysiumCore.utils.ManagerRegistry;
import org.bson.Document;
import org.bukkit.entity.Player;

import static com.mongodb.client.model.Filters.eq;

@ManagerInterface(name = "eco")
public class EcoManager {
    private final MongoCollection<Document> ecoCollection;

    public EcoManager() {
        MongoDBManager mongoDBManager = ManagerRegistry.get("mongodb");
        MongoDatabase database = mongoDBManager.getDatabase();
        this.ecoCollection = database.getCollection("economy");
    }

    public boolean isPlayerInDatabase(Player player) {
        Document query = new Document("uuid", player.getUniqueId().toString());
        return ecoCollection.find(query).first() != null;
    }

    public void initPlayer(Player player) {
        Document query = new Document("uuid", player.getUniqueId().toString())
                .append("name", player.getDisplayName().toString())
                .append("balance", 1000);
        ecoCollection.insertOne(query);
        ElysiumLogger.log("Init ECO for player: " + player.getDisplayName());
    }
    
    public boolean hasValidBalance(Player sender, Integer amount) {
        Document query = new Document("uuid", sender.getUniqueId().toString());
        Document result = ecoCollection.find(query).first();
        if (result != null) {
            int balance = result.getInteger("balance");
            return balance >= amount;
        }
        return false;
    }

    public void transfere(Player sender, Player receiver, Integer amount) {
        ecoCollection.updateOne(eq("uuid", sender.getUniqueId().toString()),
                Updates.inc("balance", -amount));
        ecoCollection.updateOne(eq("uuid", receiver.getUniqueId().toString()),
                Updates.inc("balance", amount));
        ElysiumLogger.log(sender.getDisplayName() + " transfers " + amount + "COL to " + receiver.getDisplayName());
    }

    public Integer getPlayerBalance(Player player) {
        Document query = new Document("uuid", player.getUniqueId().toString());
        Document result = ecoCollection.find(query).first();
        return result.getInteger("balance");
    }

    public void addBalance(Player player, Integer amount) {
        ecoCollection.updateOne(eq("uuid", player.getUniqueId().toString()),
                Updates.inc("balance", amount));
    }

    public void setBalance(Player player, Integer amount) {
        ecoCollection.updateOne(eq("uuid", player.getUniqueId().toString()),
                Updates.set("balance", amount));
    }
}
