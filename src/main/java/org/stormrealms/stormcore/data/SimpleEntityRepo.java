package org.stormrealms.stormcore.data;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.stormrealms.stormcore.model.SimpleEntity;

@Repository
public interface SimpleEntityRepo extends CrudRepository<SimpleEntity, UUID> {
		
}
