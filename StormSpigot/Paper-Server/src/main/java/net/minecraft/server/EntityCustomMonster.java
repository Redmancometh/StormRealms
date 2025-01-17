package net.minecraft.server;

import org.bukkit.entity.RPGEntity;
import org.stormrealms.stormspigot.entities.RPGEntityData;
import lombok.Getter;

public class EntityCustomMonster extends EntityMonster {
	@Getter
	private RPGEntityData data;

	public EntityCustomMonster(EntityTypes<? extends EntityMonster> entitytypes, World world, RPGEntityData data) {
		super(entitytypes, world);
		this.data = data;
	}

	@Override
	public Packet<?> L() {
		return new PacketPlayOutSpawnEntityLiving(this);
	}

	@Override
	public void tick() {
		super.tick();
		((RPGEntity) getBukkitEntity()).tick();
	}

}
