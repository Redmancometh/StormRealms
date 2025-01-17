package org.bukkit.craftbukkit.entity;

import java.util.UUID;
import net.minecraft.server.EntityTameableAnimal;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Tameable;

public class CraftTameableAnimal extends CraftAnimals implements Tameable, Creature {
    public CraftTameableAnimal(CraftServer server, EntityTameableAnimal entity) {
        super(server, entity);
    }

    @Override
    public EntityTameableAnimal getHandle() {
        return (EntityTameableAnimal) super.getHandle();
    }

    public UUID getOwnerUniqueId() {
        return getOwnerUUID();
    }
    public UUID getOwnerUUID() {
        try {
            return getHandle().getOwnerUUID();
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public void setOwnerUUID(UUID uuid) {
        getHandle().setOwnerUUID(uuid);
    }

    @Override
    public AnimalTamer getOwner() {
        if (getOwnerUUID() == null) {
            return null;
        }

        AnimalTamer owner = getServer().getPlayer(getOwnerUUID());
        if (owner == null) {
            owner = getServer().getOfflinePlayer(getOwnerUUID());
        }

        return owner;
    }

    @Override
    public boolean isTamed() {
        return getHandle().isTamed();
    }

    @Override
    public void setOwner(AnimalTamer tamer) {
        if (tamer != null) {
            setTamed(true);
            getHandle().setGoalTarget(null, null, false);
            setOwnerUUID(tamer.getUniqueId());
        } else {
            setTamed(false);
            setOwnerUUID(null);
        }
    }

    @Override
    public void setTamed(boolean tame) {
        getHandle().setTamed(tame);
        if (!tame) {
            setOwnerUUID(null);
        }
    }

    public boolean isSitting() {
        return getHandle().isSitting();
    }

    public void setSitting(boolean sitting) {
        getHandle().setSitting(sitting);
        getHandle().getGoalSit().setSitting(sitting);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{owner=" + getOwner() + ",tamed=" + isTamed() + "}";
    }
}
