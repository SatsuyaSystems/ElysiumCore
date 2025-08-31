package de.satsuya.elysiumCore;

import de.satsuya.elysiumCore.manager.EcoManager;
import de.satsuya.elysiumCore.manager.GuildManager;
import de.satsuya.elysiumCore.manager.LuckPermsNametagSubscriber;
import de.satsuya.elysiumCore.manager.NametagService;
import de.satsuya.elysiumCore.utils.*;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ElysiumCore extends JavaPlugin {

    private static ElysiumCore instance;
    private NametagService nametagService;

    private static MongoDBManager mongoDBManager;
    public static GuildManager guildManager;
    public static EcoManager ecoManager;

    @Override
    public void onEnable() {
        instance = this;

        ElysiumLogger.log("ElysiumCore is starting...");
        try {
            ConfigLoader.setupConfig();
            ConfigLoader.loadConfig();
            ElysiumLogger.log("Configuration successfully loaded.");

            String mongoUri = ConfigLoader.configData.getString("mongodb.uri");
            String databaseName = ConfigLoader.configData.getString("mongodb.database");

            mongoDBManager = new MongoDBManager(mongoUri, databaseName);
            guildManager = new GuildManager();
            ecoManager = new EcoManager();

            if (ConfigLoader.configData.getBoolean("debug")) {
                ElysiumLogger.debug("Debug mode is enabled.");
            }

            // Stelle sicher, dass LuckPerms aktiv ist
            var luckPermsPlugin = Bukkit.getPluginManager().getPlugin("LuckPerms");
            if (luckPermsPlugin == null || !luckPermsPlugin.isEnabled()) {
                getLogger().severe("LuckPerms ist nicht geladen/aktiv. Deaktiviere ElysiumCore.");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            LuckPerms luckPerms = LuckPermsProvider.get();

            // NametagService ohne ProtocolLib initialisieren
            this.nametagService = new NametagService(this, luckPerms);

            // LuckPerms EventSubscriber initialisieren
            new LuckPermsNametagSubscriber(luckPerms, nametagService);

            // Bereits online befindliche Spieler direkt synchronisieren
            Bukkit.getScheduler().runTask(this, () -> {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    nametagService.onJoin(p);
                }
            });


            EventLoader.loadEvents(this);
            ElysiumLogger.log("Events successfully loaded.");

            CommandLoader.loadCommands(this);
            ElysiumLogger.log("Commands successfully loaded.");

            TaskLoader.loadAndStartRunnables(this);
            ElysiumLogger.log("Tasks successfully loaded and started.");

        } catch (Exception e) {
            ElysiumLogger.error("Error loading plugin components: " + e.getMessage());
            e.printStackTrace();
        }

        ElysiumLogger.log("ElysiumCore has been successfully activated!");
    }

    @Override
    public void onDisable() {
        ElysiumLogger.log("ElysiumCore is shutting down...");
        ElysiumLogger.log("Additional bugs Sponsored by angryzero.");
        ElysiumLogger.log("ElysiumCore has been shut down.");
    }

    public static MongoDBManager getMongoDBManager() {
        return mongoDBManager;
    }
    public static GuildManager getGuildManager() { return guildManager; }
    public static EcoManager getEcoManager() { return ecoManager; }
    public NametagService getNametagService() {
        return nametagService;
    }


    public static ElysiumCore getInstance() {
        return instance;
    }
}