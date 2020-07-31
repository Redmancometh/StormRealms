package org.stormrealms.stormmobs.entity.abs;

import java.util.function.Function;
import java.util.function.Supplier;

import org.stormrealms.stormmobs.entity.LootableEntity;
import org.stormrealms.stormmobs.entity.RPGEntity;

import lombok.Setter;
import net.minecraft.server.EntityCaveSpider;
import net.minecraft.server.EntityTypes;
import net.minecraft.server.World;

public class CustomCaveSpider<T extends CustomCaveSpider> extends EntityCaveSpider
		implements RPGEntity<EntityCaveSpider, CustomCaveSpider>, LootableEntity {
	@Setter
	public static Supplier<String> nameSupplier;
	private static Function<World, CustomCaveSpider> spawnSupplier = (w) -> new CustomCaveSpider(w);

	public CustomCaveSpider(World world) {
		super(EntityTypes.CAVE_SPIDER, world);
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
	public Function<World, CustomCaveSpider> spawnSupplier() {
		return spawnSupplier;
	}

	@Override
	public EntityCaveSpider getEntity() {
		return this;
	}

	@Override
	public int getDefense() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

}
