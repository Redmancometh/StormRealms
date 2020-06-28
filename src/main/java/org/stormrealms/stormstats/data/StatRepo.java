package org.stormrealms.stormstats.data;

import java.util.UUID;
import org.springframework.stereotype.Component;
import org.stormrealms.mediators.ObjectManager;
import org.stormrealms.stormstats.model.RPGPlayer;

@Component
public class StatRepo extends ObjectManager<UUID, RPGPlayer> {

	public StatRepo() {
		super(RPGPlayer.class);
	}

}
