package de.satsuya.elysiumCore.utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SetHolder {
    public static Set<UUID> vanishedPlayers = new HashSet<>();
    public static final Map<UUID, String> playerGuildMap = new ConcurrentHashMap<>();
}
