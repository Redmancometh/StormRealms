package org.stormrealms.stormmobs.entity.abs;

import java.util.function.Function;
import java.util.function.Supplier;
import org.stormrealms.stormmobs.entity.RPGEntity;
import lombok.Setter;
import net.minecraft.server.EntitySpider;
import net.minecraft.server.EntityTypes;
import net.minecraft.server.World;

public class CustomSpider<T extends CustomSpider> extends EntitySpider
		implements RPGEntity<EntitySpider, CustomSpider> {
	@Setter
	public static Supplier<String> nameSupplier;
	private static Function<World, CustomSpider> spawnSupplier = (w) -> new CustomSpider(w);

	public CustomSpider(World world) {
		super(EntityTypes.SPIDER, world);
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
	public Function<World, CustomSpider> spawnSupplier() {
		return spawnSupplier;
	}

	@Override
	public EntitySpider getEntity() {
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
