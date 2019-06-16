package org.stormrealms.stormstats.data;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.stormrealms.stormstats.model.RPGPlayer;

public interface RPGPlayerRepository extends JpaRepository<RPGPlayer, UUID> {
	
}
