package de.satsuya.elysiumCore.manager;

import de.satsuya.elysiumCore.utils.SetHolder;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class NametagService {

    private final Plugin plugin;
    private final LuckPerms luckPerms;

    private final Map<UUID, NametagData> cache = new ConcurrentHashMap<>();

    public NametagService(Plugin plugin, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
    }

    // Aufrufen bei PlayerJoinEvent
    public void onJoin(Player joined) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.canSee(joined)) continue;
            ensureTeamForTargetVisibleToViewer(joined, viewer);
        }
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.equals(joined) || !joined.canSee(target)) continue;
            ensureTeamForTargetVisibleToViewer(target, joined);
        }
    }

    // Aufrufen bei PlayerQuitEvent
    public void onQuit(Player quit) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            removeFromViewer(quit.getUniqueId(), quit.getName(), viewer);
        }
        cache.remove(quit.getUniqueId());
    }

    // Aufrufen bei LuckPerms-Änderung des Users
    public void updateNametagFor(UUID targetId) {
        Player target = Bukkit.getPlayer(targetId);
        if (target == null || !target.isOnline()) return;

        CompletableFuture
                .supplyAsync(() -> fetchNametagFromLuckPerms(target))
                .thenAccept(fresh -> {
                    NametagData prev = cache.get(targetId);
                    if (fresh.equals(prev)) return;
                    cache.put(targetId, fresh);

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        for (Player viewer : Bukkit.getOnlinePlayers()) {
                            if (!viewer.canSee(target)) continue;
                            createOrUpdateForViewer(target, viewer, fresh);
                        }
                    });
                });
    }

    private void ensureTeamForTargetVisibleToViewer(Player target, Player viewer) {
        NametagData data = cache.computeIfAbsent(target.getUniqueId(), id -> fetchNametagFromLuckPerms(target));
        createOrUpdateForViewer(target, viewer, data);
    }

    private String teamNameFor(UUID targetId) {
        return "nt-" + targetId.toString().replace("-", "");
    }

    private NametagData fetchNametagFromLuckPerms(Player p) {
        String prefix = "";
        String suffix = "";
        ChatColor color = ChatColor.RESET;

        User user = luckPerms.getUserManager().getUser(p.getUniqueId());
        if (user != null) {
            QueryOptions queryOptions = luckPerms.getContextManager().getQueryOptions(p);
            var meta = user.getCachedData().getMetaData(queryOptions);
            if (meta != null) {
                if (meta.getPrefix() != null) prefix = ChatColor.translateAlternateColorCodes('&', meta.getPrefix());
                if (meta.getSuffix() != null) suffix = ChatColor.translateAlternateColorCodes('&', meta.getSuffix());
            }
        }

        // Gildenname aus dem Cache an den Suffix anhängen (z. B. " [Guild]")
        String guildName = SetHolder.playerGuildMap.get(p.getUniqueId());
        if (guildName != null && !guildName.isEmpty()) {
            String guildTag = ChatColor.BLUE + " [" + guildName + "]";
            suffix = (suffix == null || suffix.isEmpty()) ? guildTag : (suffix + guildTag);
        }

        return new NametagData(prefix, suffix, color);
    }


    private void createOrUpdateForViewer(Player target, Player viewer, NametagData data) {
        Scoreboard sb = getScoreboard(viewer);
        String teamName = teamNameFor(target.getUniqueId());

        Team team = sb.getTeam(teamName);
        if (team == null) {
            team = sb.registerNewTeam(teamName);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setCanSeeFriendlyInvisibles(false);
            team.setAllowFriendlyFire(true);
        }

        // Prefix/Suffix setzen (Legacy-Farbcodes werden unterstützt)
        team.setPrefix(data.prefix());
        team.setSuffix(data.suffix());

        // Eintrag hinzufügen
        if (!team.hasEntry(target.getName())) {
            team.addEntry(target.getName());
        }
    }

    private void removeFromViewer(UUID targetId, String targetName, Player viewer) {
        Scoreboard sb = getScoreboard(viewer);
        Team team = sb.getTeam(teamNameFor(targetId));
        if (team == null) return;

        if (team.hasEntry(targetName)) {
            team.removeEntry(targetName);
        }
        if (team.getEntries().isEmpty()) {
            team.unregister();
        }
    }

    private Scoreboard getScoreboard(Player viewer) {
        Scoreboard sb = viewer.getScoreboard();
        if (sb == null) {
            ScoreboardManager sm = Bukkit.getScoreboardManager();
            sb = sm != null ? sm.getMainScoreboard() : Bukkit.getScoreboardManager().getMainScoreboard();
            viewer.setScoreboard(sb);
        }
        return sb;
    }

    public record NametagData(String prefix, String suffix, ChatColor color) { }
}
