package org.stormrealms.stormmobs.entity.abs;

import java.util.function.Function;
import java.util.function.Supplier;
import org.stormrealms.stormmobs.entity.RPGEntity;
import lombok.Setter;
import net.minecraft.server.EntityTypes;
import net.minecraft.server.EntityZombie;
import net.minecraft.server.World;

public class CustomZombie<T extends CustomZombie> extends EntityZombie
		implements RPGEntity<EntityZombie, CustomZombie> {
	@Setter
	public static Supplier<String> nameSupplier;
	private static Function<World, CustomZombie> spawnSupplier = (w) -> new CustomZombie(w);

	public CustomZombie(World world) {
		super(EntityTypes.ZOMBIE, world);
	}

	@Override
	public void onSpawn() {
	}

	@Override
	public void tick() {

	}

	@Override
	public Supplier<String> nameSupplier() {
		return nameSupplier;
	}

	/**
	 * Only way to really prevent reflection..
	 */
	@Override
	public Function<World, CustomZombie> spawnSupplier() {
		return spawnSupplier;
	}

	@Override
	public EntityZombie getEntity() {
		return this;
	}

	@Override
	public int getDefense() {
		return 0;
	}

	@Override
	public int getLevel() {
		return 0;
	}

}
