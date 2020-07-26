package org.stormrealms.stormmobs.entity.abs;

import java.util.function.Function;
import java.util.function.Supplier;
import org.stormrealms.stormmobs.entity.RPGEntity;
import lombok.Setter;
import net.minecraft.server.EntityChicken;
import net.minecraft.server.EntityTypes;
import net.minecraft.server.World;

public class CustomChicken<T extends CustomChicken> extends EntityChicken
		implements RPGEntity<EntityChicken, CustomChicken> {
	@Setter
	public static Supplier<String> nameSupplier;
	private static Function<World, CustomChicken> spawnSupplier = (w) -> new CustomChicken(w);

	public CustomChicken(World world) {
		super(EntityTypes.CHICKEN, world);
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
	public Function<World, CustomChicken> spawnSupplier() {
		return spawnSupplier;
	}

	@Override
	public EntityChicken getEntity() {
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
