package de.satsuya.elysiumCore.commands;

import de.satsuya.elysiumCore.ElysiumCore;
import de.satsuya.elysiumCore.manager.GuildManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.Map;

public class GuildCommand implements PluginCommand {

    private final GuildManager guildManager;

    /**
     * Holt die GuildManager Instanz aus der Hauptklasse.
     */
    public GuildCommand() {
        this.guildManager = ElysiumCore.getGuildManager();
    }

    @Override
    public String getName() {
        return "guild";
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        // Stellt sicher, dass der Befehl von einem Spieler ausgeführt wird
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        Player player = (Player) sender;

        // Zeigt die Hilfe, wenn keine Argumente oder das 'help'-Argument verwendet werden
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            showHelp(player);
            return true;
        }

        // Verarbeitet die Unterbefehle basierend auf dem ersten Argument
        switch (args[0].toLowerCase()) {
            case "create":
                handleCreateCommand(player, args);
                break;
            case "invite":
                handleInviteCommand(player, args);
                break;
            case "join":
                handleJoinCommand(player, args);
                break;
            case "leave":
                handleLeaveCommand(player);
                break;
            case "kick":
                handleKickCommand(player, args);
                break;
            case "list":
                handleListCommand(player);
                break;
            case "clearinvites":
                handleClearInvitesCommand(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unbekannter Unterbefehl. Nutze /" + label + " help für eine Liste der Befehle.");
                break;
        }

        return true;
    }

    /**
     * Zeigt dem Spieler die Hilfenachricht an.
     * @param player Der Spieler, dem die Hilfe gezeigt werden soll.
     */
    private void showHelp(Player player) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a----- Gilden Befehle -----"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/guild create <Name> &7- Erstellt eine neue Gilde."));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/guild invite <Spieler> &7- Lädt einen Spieler in deine Gilde ein."));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/guild join <Name> &7- Tritt einer Gilde bei, in die du eingeladen wurdest."));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/guild list &7- Zeigt eine Liste der Mitglieder deiner Gilde an."));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/guild leave &7- Verlässt deine aktuelle Gilde."));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/guild kick <Spieler> &7- Entfernt einen Spieler aus der Gilde (nur für den Gilden-Leiter)."));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/guild clearinvites &7- Löscht alle ausstehenden Einladungen deiner Gilde."));
    }

    /**
     * Verarbeitet den 'create'-Unterbefehl.
     * @param player Der Spieler, der den Befehl ausführt.
     * @param args Die Argumente des Befehls.
     */
    private void handleCreateCommand(Player player, String[] args) {
        // Prüft, ob die richtigen Argumente vorhanden sind
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Nutzung: /guild create <Name>");
            return;
        }

        String guildName = args[1];

        // Prüft, ob der Spieler bereits in einer Gilde ist
        if (guildManager.isPlayerInGuild(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Du bist bereits in einer Gilde!");
            return;
        }

        // Prüft, ob der Gilden-Name bereits existiert
        if (guildManager.doesGuildExist(guildName)) {
            player.sendMessage(ChatColor.RED + "Eine Gilde mit diesem Namen existiert bereits.");
            return;
        }

        // Versucht, die Gilde zu erstellen
        if (guildManager.createGuild(guildName, player.getUniqueId(), player.getName())) {
            player.sendMessage(ChatColor.GREEN + "Du hast die Gilde '" + guildName + "' erfolgreich erstellt!");
        } else {
            player.sendMessage(ChatColor.RED + "Fehler beim Erstellen der Gilde. Versuche es erneut.");
        }
    }

    /**
     * Verarbeitet den 'invite'-Unterbefehl.
     * @param player Der Spieler, der den Befehl ausführt.
     * @param args Die Argumente des Befehls.
     */
    private void handleInviteCommand(Player player, String[] args) {
        // Prüft, ob die richtigen Argumente vorhanden sind
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Nutzung: /guild invite <Spieler>");
            return;
        }

        // Prüft, ob der Spieler in einer Gilde ist
        String playerGuildName = guildManager.getPlayerGuild(player.getUniqueId());
        if (playerGuildName == null) {
            player.sendMessage(ChatColor.RED + "Du musst in einer Gilde sein, um jemanden einzuladen!");
            return;
        }

        // Prüft, ob der Spieler der Gilden-Leiter ist
        if (!guildManager.isGuildLeader(player.getUniqueId(), playerGuildName)) {
            player.sendMessage(ChatColor.RED + "Du musst der Gilden-Leiter sein, um Spieler einzuladen.");
            return;
        }

        // Findet den Zielspieler
        Player targetPlayer = Bukkit.getPlayer(args[1]);
        if (targetPlayer == null) {
            player.sendMessage(ChatColor.RED + "Der Spieler ist nicht online oder existiert nicht.");
            return;
        }

        // Verhindert, sich selbst einzuladen
        if (targetPlayer.equals(player)) {
            player.sendMessage(ChatColor.RED + "Du kannst dich nicht selbst einladen.");
            return;
        }

        // Prüft, ob der Zielspieler bereits in einer Gilde ist
        if (guildManager.isPlayerInGuild(targetPlayer.getUniqueId())) {
            player.sendMessage(ChatColor.RED + targetPlayer.getName() + " ist bereits in einer Gilde.");
            return;
        }

        // NEU: Prüft, ob eine Einladung bereits existiert
        if (guildManager.hasInvitation(targetPlayer.getUniqueId(), playerGuildName)) {
            player.sendMessage(ChatColor.RED + targetPlayer.getName() + " hat bereits eine ausstehende Einladung für deine Gilde.");
            return;
        }

        // Sendet die Einladung und informiert beide Spieler
        guildManager.invitePlayer(targetPlayer.getUniqueId(), playerGuildName);
        player.sendMessage(ChatColor.GREEN + "Du hast " + targetPlayer.getName() + " erfolgreich in deine Gilde '" + playerGuildName + "' eingeladen.");
        targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aDu wurdest in die Gilde &6" + playerGuildName + " &aeingeladen. Nutze &6/guild join " + playerGuildName + " &aum beizutreten."));
    }

    /**
     * Verarbeitet den 'join'-Unterbefehl.
     * @param player Der Spieler, der den Befehl ausführt.
     * @param args Die Argumente des Befehls.
     */
    private void handleJoinCommand(Player player, String[] args) {
        // Prüft, ob die richtigen Argumente vorhanden sind
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Nutzung: /guild join <Name>");
            return;
        }

        String guildToJoin = args[1];

        // Prüft, ob der Spieler bereits in einer Gilde ist
        if (guildManager.isPlayerInGuild(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Du bist bereits in einer Gilde!");
            return;
        }

        // Prüft, ob eine Einladung von dieser Gilde existiert
        if (!guildManager.hasInvitation(player.getUniqueId(), guildToJoin)) {
            player.sendMessage(ChatColor.RED + "Du hast keine ausstehende Einladung für diese Gilde oder der Gilden-Name ist falsch.");
            return;
        }

        // Fügt den Spieler zur Gilde hinzu
        guildManager.joinGuild(player.getUniqueId(), player.getName(), guildToJoin);
        player.sendMessage(ChatColor.GREEN + "Du bist der Gilde '" + guildToJoin + "' erfolgreich beigetreten!");

        // Benachrichtigt den Gildenleiter und die anderen Mitglieder
        String playerGuildName = guildManager.getPlayerGuild(player.getUniqueId());
        if (playerGuildName != null) {
            Map<String, UUID> members = guildManager.getGuildMembersWithNames(playerGuildName);
            if (members != null) {
                for (Map.Entry<String, UUID> entry : members.entrySet()) {
                    Player member = Bukkit.getPlayer(entry.getValue());
                    if (member != null && member.isOnline()) {
                        member.sendMessage(ChatColor.AQUA + player.getName() + " ist eurer Gilde beigetreten!");
                    }
                }
            }
        }
    }

    /**
     * Verarbeitet den 'leave'-Unterbefehl.
     * @param player Der Spieler, der den Befehl ausführt.
     */
    private void handleLeaveCommand(Player player) {
        // Prüft, ob der Spieler in einer Gilde ist
        String playerGuildName = guildManager.getPlayerGuild(player.getUniqueId());
        if (playerGuildName == null) {
            player.sendMessage(ChatColor.RED + "Du bist in keiner Gilde.");
            return;
        }

        // Prüft, ob der Spieler der Leiter ist
        if (guildManager.isGuildLeader(player.getUniqueId(), playerGuildName)) {
            player.sendMessage(ChatColor.RED + "Als Gilden-Leiter musst du die Gilde auflösen, um sie zu verlassen.");
            return;
        }

        // Entfernt den Spieler aus der Gilde
        guildManager.leaveGuild(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Du hast die Gilde '" + playerGuildName + "' verlassen.");
    }

    /**
     * Verarbeitet den 'kick'-Unterbefehl.
     * @param player Der Spieler, der den Befehl ausführt.
     * @param args Die Argumente des Befehls.
     */
    private void handleKickCommand(Player player, String[] args) {
        // Prüft, ob die richtigen Argumente vorhanden sind
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Nutzung: /guild kick <Spieler>");
            return;
        }

        String kickedPlayerName = args[1];
        Player kickedPlayer = Bukkit.getPlayer(kickedPlayerName);
        if (kickedPlayer == null) {
            player.sendMessage(ChatColor.RED + "Der Spieler ist nicht online oder existiert nicht.");
            return;
        }

        // Prüft, ob der Spieler in einer Gilde ist
        String playerGuildName = guildManager.getPlayerGuild(player.getUniqueId());
        if (playerGuildName == null) {
            player.sendMessage(ChatColor.RED + "Du musst in einer Gilde sein, um jemanden zu kicken!");
            return;
        }

        // Prüft, ob der Spieler der Gilden-Leiter ist
        if (!guildManager.isGuildLeader(player.getUniqueId(), playerGuildName)) {
            player.sendMessage(ChatColor.RED + "Du musst der Gilden-Leiter sein, um Spieler zu kicken.");
            return;
        }

        // Verhindert, sich selbst zu kicken
        if (kickedPlayer.equals(player)) {
            player.sendMessage(ChatColor.RED + "Du kannst dich nicht selbst kicken.");
            return;
        }

        // Prüft, ob der zu kickende Spieler in der Gilde ist
        String kickedPlayerGuild = guildManager.getPlayerGuild(kickedPlayer.getUniqueId());
        if (kickedPlayerGuild == null || !kickedPlayerGuild.equals(playerGuildName)) {
            player.sendMessage(ChatColor.RED + "Dieser Spieler ist nicht in deiner Gilde.");
            return;
        }

        // Entfernt den Spieler aus der Gilde
        guildManager.kickPlayer(kickedPlayer.getUniqueId());
        player.sendMessage(ChatColor.GREEN + kickedPlayer.getName() + " wurde erfolgreich aus der Gilde entfernt.");
        kickedPlayer.sendMessage(ChatColor.RED + "Du wurdest aus der Gilde '" + playerGuildName + "' gekickt.");
    }

    /**
     * Verarbeitet den 'list'-Unterbefehl.
     * @param player Der Spieler, der den Befehl ausführt.
     */
    private void handleListCommand(Player player) {
        String playerGuildName = guildManager.getPlayerGuild(player.getUniqueId());
        if (playerGuildName == null) {
            player.sendMessage(ChatColor.RED + "Du bist in keiner Gilde.");
            return;
        }

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a----- Gildenmitglieder von &6" + playerGuildName + " &a-----"));
        Map<String, UUID> members = guildManager.getGuildMembersWithNames(playerGuildName);

        for (Map.Entry<String, UUID> entry : members.entrySet()) {
            String memberName = entry.getKey();
            UUID memberUUID = entry.getValue();

            String status = "Mitglied";
            if (guildManager.isGuildLeader(memberUUID, playerGuildName)) {
                status = "Leiter";
            }
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &a" + memberName + " &7(&e" + status + "&7)"));
        }
    }

    /**
     * Verarbeitet den 'clearinvites'-Unterbefehl.
     * @param player Der Spieler, der den Befehl ausführt.
     */
    private void handleClearInvitesCommand(Player player) {
        String playerGuildName = guildManager.getPlayerGuild(player.getUniqueId());
        if (playerGuildName == null) {
            player.sendMessage(ChatColor.RED + "Du bist in keiner Gilde.");
            return;
        }

        if (!guildManager.isGuildLeader(player.getUniqueId(), playerGuildName)) {
            player.sendMessage(ChatColor.RED + "Nur der Gilden-Leiter kann Einladungen löschen.");
            return;
        }

        guildManager.clearInvitations(playerGuildName);
        player.sendMessage(ChatColor.GREEN + "Alle ausstehenden Einladungen deiner Gilde wurden erfolgreich gelöscht.");
    }
}
