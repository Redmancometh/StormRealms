package org.stormrealms.stormmobs.entity.abs;

import java.util.function.Function;
import java.util.function.Supplier;
import org.stormrealms.stormmobs.entity.RPGEntity;
import lombok.Setter;
import net.minecraft.server.EntitySkeleton;
import net.minecraft.server.EntityTypes;
import net.minecraft.server.World;

public class CustomSkeleton<T extends CustomSkeleton> extends EntitySkeleton
		implements RPGEntity<EntitySkeleton, CustomSkeleton> {
	@Setter
	public static Supplier<String> nameSupplier;
	private static Function<World, CustomSkeleton> spawnSupplier = (w) -> new CustomSkeleton(w);

	public CustomSkeleton(World world) {
		super(EntityTypes.SKELETON, world);
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
	public Function<World, CustomSkeleton> spawnSupplier() {
		return spawnSupplier;
	}

	@Override
	public EntitySkeleton getEntity() {
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
