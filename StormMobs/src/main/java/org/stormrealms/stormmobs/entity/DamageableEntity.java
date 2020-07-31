package org.stormrealms.stormmobs.entity;

import org.bukkit.entity.LivingEntity;

public interface DamageableEntity
{
    public abstract void onGotHit(LivingEntity damager);

    public abstract void onHit(LivingEntity damaged);

    public abstract void onDeath();
}
