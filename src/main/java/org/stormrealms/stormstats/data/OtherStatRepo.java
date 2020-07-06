package org.stormrealms.stormstats.data;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.stormrealms.stormstats.model.RPGPlayer;

@Repository
public interface OtherStatRepo extends JpaRepository<RPGPlayer, UUID> {

}
