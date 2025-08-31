package de.satsuya.elysiumCore.manager;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.user.UserDataRecalculateEvent;

import java.util.UUID;

public class LuckPermsNametagSubscriber {

    public LuckPermsNametagSubscriber(LuckPerms luckPerms, NametagService service) {
        luckPerms.getEventBus().subscribe(UserDataRecalculateEvent.class, event -> {
            UUID uuid = event.getUser().getUniqueId();
            service.updateNametagFor(uuid);
        });
    }
}
