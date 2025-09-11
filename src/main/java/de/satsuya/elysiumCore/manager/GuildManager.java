package de.satsuya.elysiumCore.manager;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import de.satsuya.elysiumCore.interfaces.ManagerInterface;
import de.satsuya.elysiumCore.utils.ManagerRegistry;
import org.bson.Document;

import java.util.*;

/**
 * Diese Klasse verwaltet alle Gilden- und Spielerdaten in einer MongoDB-Datenbank.
 * Sie verwendet die zentrale MongoDBManager-Instanz aus der Hauptklasse ElysiumCore.
 */
@ManagerInterface(name = "guild")
public class GuildManager {
    private final MongoCollection<Document> guildCollection;
    private final MongoCollection<Document> invitationCollection;
    // Die playerDataCollection wird nicht mehr benötigt, da die Spielernamen direkt in der Gilde gespeichert werden.

    /**
     * Erstellt eine neue Instanz des GuildManager.
     * Nutzt die zentrale MongoDBManager-Instanz aus der Hauptklasse.
     */
    public GuildManager() {
        MongoDBManager mongoDBManager = ManagerRegistry.get("mongodb");
        MongoDatabase database = mongoDBManager.getDatabase();
        this.guildCollection = database.getCollection("guilds");
        this.invitationCollection = database.getCollection("invitations");
    }

    /**
     * Erstellt eine neue Gilde in der Datenbank.
     * @param guildName Der Name der neuen Gilde.
     * @param leader Die UUID des Gilden-Leiters.
     * @param leaderName Der Name des Gilden-Leiters.
     * @return True, wenn die Gilde erfolgreich erstellt wurde, sonst false.
     */
    public boolean createGuild(String guildName, UUID leader, String leaderName) {
        // Zuerst prüfen, ob die Gilde oder der Spieler bereits existiert, um Fehler zu vermeiden.
        if (doesGuildExist(guildName) || isPlayerInGuild(leader)) {
            return false;
        }

        // Ein Dokument für den Leiter erstellen, das UUID und Namen enthält.
        Document leaderMemberDoc = new Document("uuid", leader.toString())
                .append("name", leaderName);

        // Ein neues Gilden-Dokument erstellen, das die Mitglieder-Dokumente speichert.
        Document guildDoc = new Document("guildName", guildName)
                .append("leaderUUID", leader.toString())
                .append("members", Collections.singletonList(leaderMemberDoc));

        try {
            guildCollection.insertOne(guildDoc);
            return true;
        } catch (Exception e) {
            // Fehlermeldung loggen, falls etwas schiefgeht
            System.err.println("Fehler beim Erstellen der Gilde: " + e.getMessage());
            return false;
        }
    }

    /**
     * Prüft, ob ein Spieler in einer Gilde ist, indem er die Datenbank abfragt.
     * @param playerUUID Die UUID des Spielers.
     * @return True, wenn der Spieler in einer Gilde ist, sonst false.
     */
    public boolean isPlayerInGuild(UUID playerUUID) {
        // Die Abfrage sucht in der 'members'-Liste nach einem Dokument mit der passenden UUID.
        Document query = new Document("members.uuid", playerUUID.toString());
        return guildCollection.find(query).first() != null;
    }

    /**
     * Gibt den Namen der Gilde zurück, in der sich ein Spieler befindet.
     * @param playerUUID Die UUID des Spielers.
     * @return Der Gilden-Name oder null, wenn der Spieler in keiner Gilde ist.
     */
    public String getPlayerGuild(UUID playerUUID) {
        Document query = new Document("members.uuid", playerUUID.toString());
        Document guildDoc = guildCollection.find(query).first();
        if (guildDoc != null) {
            return guildDoc.getString("guildName");
        }
        return null;
    }

    /**
     * Prüft, ob ein Gilden-Name bereits verwendet wird.
     * @param guildName Der Name der Gilde.
     * @return True, wenn der Name verwendet wird, sonst false.
     */
    public boolean doesGuildExist(String guildName) {
        return guildCollection.find(new Document("guildName", guildName)).first() != null;
    }

    /**
     * Prüft, ob der Spieler der Leiter der Gilde ist.
     * @param playerUUID Die UUID des Spielers.
     * @param guildName Der Name der Gilde.
     * @return True, wenn der Spieler der Leiter ist, sonst false.
     */
    public boolean isGuildLeader(UUID playerUUID, String guildName) {
        Document query = new Document("guildName", guildName)
                .append("leaderUUID", playerUUID.toString());
        return guildCollection.find(query).first() != null;
    }

    /**
     * Löst eine Einladung an einen Spieler aus.
     * @param invitedPlayerUUID Die UUID des eingeladenen Spielers.
     * @param guildName Der Name der Gilde, die die Einladung ausspricht.
     */
    public void invitePlayer(UUID invitedPlayerUUID, String guildName) {
        // Ein Einladungsdokument in der invitations-Collection speichern.
        Document invitationDoc = new Document("playerUUID", invitedPlayerUUID.toString())
                .append("guildName", guildName);
        invitationCollection.insertOne(invitationDoc);
    }

    /**
     * Prüft, ob eine Einladung für einen Spieler von einer bestimmten Gilde vorliegt.
     * @param playerUUID Die UUID des Spielers.
     * @param guildName Der Name der Gilde.
     * @return True, wenn eine Einladung vorliegt, sonst false.
     */
    public boolean hasInvitation(UUID playerUUID, String guildName) {
        Document query = new Document("playerUUID", playerUUID.toString())
                .append("guildName", guildName);
        return invitationCollection.find(query).first() != null;
    }

    /**
     * Fügt einen Spieler zu einer Gilde hinzu und löscht die Einladung.
     * @param playerUUID Die UUID des Spielers, der beitritt.
     * @param playerName Der Name des Spielers.
     * @param guildName Der Name der Gilde.
     */
    public void joinGuild(UUID playerUUID, String playerName, String guildName) {
        // Ein Dokument für den neuen Spieler erstellen.
        Document newMemberDoc = new Document("uuid", playerUUID.toString())
                .append("name", playerName);

        // Fügt das Dokument zur Mitgliederliste der Gilde hinzu.
        Document updateQuery = new Document("guildName", guildName);
        guildCollection.updateOne(updateQuery, Updates.push("members", newMemberDoc));

        // Löscht die Einladung aus der invitations-Collection.
        Document deleteQuery = new Document("playerUUID", playerUUID.toString())
                .append("guildName", guildName);
        invitationCollection.deleteOne(deleteQuery);
    }

    /**
     * Entfernt einen Spieler aus seiner Gilde.
     * @param playerUUID Die UUID des Spielers, der entfernt wird.
     */
    public void leaveGuild(UUID playerUUID) {
        // Findet die Gilde, in der der Spieler ist.
        String guildName = getPlayerGuild(playerUUID);
        if (guildName != null) {
            // Entfernt das Spieler-Dokument aus der Mitgliederliste der Gilde.
            Document updateQuery = new Document("guildName", guildName);
            Document memberToRemove = new Document("uuid", playerUUID.toString());
            guildCollection.updateOne(updateQuery, Updates.pull("members", memberToRemove));
        }
    }

    /**
     * Entfernt einen Spieler aus seiner Gilde. Diese Methode wird für den 'kick'-Befehl verwendet.
     * @param playerUUID Die UUID des Spielers, der gekickt wird.
     */
    public void kickPlayer(UUID playerUUID) {
        leaveGuild(playerUUID);
    }

    /**
     * Gibt eine Liste aller Mitglieder-UUIDs einer Gilde zurück.
     * @param guildName Der Name der Gilde.
     * @return Eine Liste von UUIDs oder eine leere Liste, wenn die Gilde nicht existiert.
     */
    public List<UUID> getGuildMembers(String guildName) {
        Document guildDoc = guildCollection.find(new Document("guildName", guildName)).first();
        if (guildDoc != null && guildDoc.containsKey("members")) {
            List<Document> memberDocs = (List<Document>) guildDoc.get("members");
            List<UUID> memberUUIDs = new ArrayList<>();
            for (Document memberDoc : memberDocs) {
                try {
                    memberUUIDs.add(UUID.fromString(memberDoc.getString("uuid")));
                } catch (IllegalArgumentException e) {
                    System.err.println("Ungültige UUID im Gilden-Dokument: " + memberDoc.getString("uuid"));
                }
            }
            return memberUUIDs;
        }
        return Collections.emptyList();
    }

    /**
     * Gibt eine Liste aller Mitglieder einer Gilde mit ihren Namen und UUIDs zurück.
     * @param guildName Der Name der Gilde.
     * @return Eine Map mit den Namen der Mitglieder als Keys und den UUIDs als Values.
     */
    public Map<String, UUID> getGuildMembersWithNames(String guildName) {
        Document guildDoc = guildCollection.find(new Document("guildName", guildName)).first();
        if (guildDoc != null && guildDoc.containsKey("members")) {
            List<Document> memberDocs = (List<Document>) guildDoc.get("members");
            Map<String, UUID> members = new HashMap<>();

            for (Document memberDoc : memberDocs) {
                String playerName = memberDoc.getString("name");
                String uuidString = memberDoc.getString("uuid");
                members.put(playerName, UUID.fromString(uuidString));
            }
            return members;
        }
        return Collections.emptyMap();
    }

    /**
     * Gibt den Namen eines Spielers anhand seiner UUID zurück.
     * @param playerUUID Die UUID des Spielers.
     * @return Der Spielername oder null, wenn er nicht gefunden wird.
     */
    public String getPlayerName(UUID playerUUID) {
        Document guildDoc = guildCollection.find(new Document("members.uuid", playerUUID.toString())).first();
        if (guildDoc != null) {
            List<Document> memberDocs = (List<Document>) guildDoc.get("members");
            for (Document memberDoc : memberDocs) {
                if (memberDoc.getString("uuid").equals(playerUUID.toString())) {
                    return memberDoc.getString("name");
                }
            }
        }
        return null;
    }

    /**
     * Löscht alle ausstehenden Einladungen für eine Gilde.
     * @param guildName Der Name der Gilde, deren Einladungen gelöscht werden sollen.
     */
    public void clearInvitations(String guildName) {
        Document deleteQuery = new Document("guildName", guildName);
        invitationCollection.deleteMany(deleteQuery);
    }

    /**
     * Löscht eine Gilde aus der Datenbank, einschließlich aller ausstehenden Einladungen.
     * Nur der Gildenleiter kann eine Gilde löschen.
     * @param guildName Der Name der Gilde.
     * @param leaderUUID Die UUID des Spielers, der versucht, die Gilde zu löschen.
     * @return True, wenn die Gilde erfolgreich gelöscht wurde, sonst false.
     */
    public boolean deleteGuild(String guildName, UUID leaderUUID) {
        // Stellt sicher, dass die Gilde existiert und der Spieler der Leiter ist.
        Document query = new Document("guildName", guildName)
                .append("leaderUUID", leaderUUID.toString());

        if (guildCollection.find(query).first() == null) {
            // Die Gilde existiert nicht oder der Spieler ist nicht der Leiter.
            return false;
        }

        try {
            // Löscht das Gilden-Dokument.
            guildCollection.deleteOne(query);

            // Löscht alle ausstehenden Einladungen für diese Gilde.
            clearInvitations(guildName);

            return true;
        } catch (Exception e) {
            System.err.println("Fehler beim Löschen der Gilde: " + e.getMessage());
            return false;
        }
    }

    public Map<UUID, String> getAllPlayerGuildMappings() {
        Map<UUID, String> result = new HashMap<>();
        for (Document guildDoc : guildCollection.find()) {
            String guildName = guildDoc.getString("guildName");
            if (guildName == null) continue;

            Object membersObj = guildDoc.get("members");
            if (!(membersObj instanceof List<?>)) continue;

            List<?> members = (List<?>) membersObj;
            for (Object o : members) {
                if (o instanceof Document) {
                    Document memberDoc = (Document) o;
                    String uuidStr = memberDoc.getString("uuid");
                    if (uuidStr == null) continue;
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        result.put(uuid, guildName);
                    } catch (IllegalArgumentException ignored) {
                        // Ungültige UUID im Dokument – überspringen
                    }
                }
            }
        }
        return result;
    }
}
