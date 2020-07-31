package org.stormrealms.stormmobs.entity.abs;

import java.util.function.Function;
import java.util.function.Supplier;

import org.stormrealms.stormmobs.entity.LootableEntity;
import org.stormrealms.stormmobs.entity.RPGEntity;

import lombok.Setter;
import net.minecraft.server.EntityItem;
import net.minecraft.server.EntityTypes;
import net.minecraft.server.World;

public class CustomItem<T extends CustomItem> extends EntityItem
		implements RPGEntity<EntityItem, CustomItem>, LootableEntity {
	@Setter
	public static Supplier<String> nameSupplier;
	private static Function<World, CustomItem> spawnSupplier = (w) -> new CustomItem(w);

	public CustomItem(World world) {
		super(EntityTypes.ITEM, world);
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
	public Function<World, CustomItem> spawnSupplier() {
		return spawnSupplier;
	}

	@Override
	public EntityItem getEntity() {
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
