package org.stormrealms.stormmobs.entity.abs;

import java.util.function.Function;
import java.util.function.Supplier;
import org.stormrealms.stormmobs.entity.RPGEntity;
import lombok.Setter;
import net.minecraft.server.EntityTypes;
import net.minecraft.server.EntityVillager;
import net.minecraft.server.World;

public class CustomVillager<T extends CustomVillager> extends EntityVillager
		implements RPGEntity<EntityVillager, CustomVillager> {
	@Setter
	public static Supplier<String> nameSupplier;
	private static Function<World, CustomVillager> spawnSupplier = (w) -> new CustomVillager(w);

	public CustomVillager(World world) {
		super(EntityTypes.VILLAGER, world);
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
	public Function<World, CustomVillager> spawnSupplier() {
		return spawnSupplier;
	}

	@Override
	public EntityVillager getEntity() {
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
