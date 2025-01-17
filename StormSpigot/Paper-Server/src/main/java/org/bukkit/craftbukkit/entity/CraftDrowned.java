package org.bukkit.craftbukkit.entity;

import com.destroystokyo.paper.entity.CraftRangedEntity;
import net.minecraft.server.EntityDrowned;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.EntityType;

public class CraftDrowned extends CraftZombie implements Drowned, CraftRangedEntity<EntityDrowned> { // Paper

    public CraftDrowned(CraftServer server, EntityDrowned entity) {
        super(server, entity);
    }

    @Override
    public EntityDrowned getHandle() {
        return (EntityDrowned) entity;
    }

    @Override
    public String toString() {
        return "CraftDrowned";
    }

    @Override
    public EntityType getType() {
        return EntityType.DROWNED;
    }
}
