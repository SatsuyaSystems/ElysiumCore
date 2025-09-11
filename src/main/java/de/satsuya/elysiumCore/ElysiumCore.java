package de.satsuya.elysiumCore;

import de.satsuya.elysiumCore.loaders.*;
import de.satsuya.elysiumCore.manager.*;
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
            ManagerRegistry.register("mongodb", mongoDBManager);
            ElysiumLogger.log("Manager MongoDBManager (mongodb) loaded and registert");

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
            ManagerRegistry.register("nametag", this.nametagService);
            ElysiumLogger.log("Manager NametagService (nametag) loaded and registert");

            // LuckPerms EventSubscriber initialisieren
            new LuckPermsNametagSubscriber(luckPerms, nametagService);

            // Bereits online befindliche Spieler direkt synchronisieren
            Bukkit.getScheduler().runTask(this, () -> {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    nametagService.onJoin(p);
                }
            });

            ManagerLoader.loadManagers();
            ElysiumLogger.log("Managers successfully loaded.");

            EventLoader.loadEvents(this);
            ElysiumLogger.log("Events successfully loaded.");

            CommandLoader.loadCommands(this);

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

    public static ElysiumCore getInstance() {
        return instance;
    }
}