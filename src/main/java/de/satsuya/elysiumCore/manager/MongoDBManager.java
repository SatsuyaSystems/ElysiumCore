// MongoDBManager.java
package de.satsuya.elysiumCore.manager;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import de.satsuya.elysiumCore.interfaces.ManagerInterface;
import de.satsuya.elysiumCore.utils.ElysiumLogger;

@ManagerInterface(
        name = "mongodb",
        params = {"${config:mongodb.uri}", "${config:mongodb.database}"},
        weight = 100
)
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
}
