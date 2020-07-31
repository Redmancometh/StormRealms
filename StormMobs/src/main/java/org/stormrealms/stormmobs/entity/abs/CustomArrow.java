package org.stormrealms.stormmobs.entity.abs;

import java.util.function.Function;
import java.util.function.Supplier;

import org.stormrealms.stormmobs.entity.RPGEntity;

import lombok.Setter;
import net.minecraft.server.EntityArrow;
import net.minecraft.server.EntityTypes;
import net.minecraft.server.ItemStack;
import net.minecraft.server.World;

public class CustomArrow<T extends CustomArrow> extends EntityArrow implements RPGEntity<EntityArrow, T> {
	@Setter
	public static Supplier<String> nameSupplier;
	private static Function<World, CustomArrow> spawnSupplier = (w) -> new CustomArrow(w);

	public boolean itemSet = false;

	public CustomArrow(World world) {
		super(EntityTypes.ARROW, world);
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

	@Override
	public EntityArrow getEntity() {
		return this;
	}

	@Override
	public Function<World, T> spawnSupplier() {
		return (Function<World, T>) spawnSupplier;
	}

	@Override
	public int getDefense() {
		return 0;
	}

	@Override
	public int getLevel() {
		return 0;
	}

	@Override
	protected ItemStack getItemStack() {
		return null;
	}

}
