package de.satsuya.elysiumCore.manager;

import de.satsuya.elysiumCore.utils.ElysiumLogger;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.user.UserDataRecalculateEvent;

import java.util.UUID;

public class LuckPermsNametagSubscriber {

    public LuckPermsNametagSubscriber(LuckPerms luckPerms, NametagService service) {
        ElysiumLogger.log("Subscribing to LuckPerms UserDataRecalculateEvent for Nametag updates.");
        luckPerms.getEventBus().subscribe(UserDataRecalculateEvent.class, event -> {
            UUID uuid = event.getUser().getUniqueId();
            service.updateNametagFor(uuid);
        });
    }
}
