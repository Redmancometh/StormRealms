package org.stormrealms.stormmobs.entity.mixin;

import java.util.function.Function;
import java.util.function.Supplier;

import org.bukkit.craftbukkit.CraftWorld;
import org.stormrealms.stormmobs.entity.CustomEntity;
import org.stormrealms.stormmobs.entity.CustomLootable;

import lombok.Setter;
import net.minecraft.server.EntityZombie;
import net.minecraft.server.World;

public class CustomZombie<T extends CustomZombie> extends EntityZombie
		implements CustomEntity<EntityZombie, T>, CustomLootable {
	@Setter
	public static Supplier<String> nameSupplier;
	private static Function<org.bukkit.World, CustomZombie> spawnSupplier = (w) -> new CustomZombie(
			((CraftWorld) w).getHandle());

	public CustomZombie(World world) {
		super(world);
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
	public Function<org.bukkit.World, T> spawnSupplier() {
		return (Function<org.bukkit.World, T>) spawnSupplier;
	}

	@Override
	public EntityZombie getEntity() {
		return this;
	}


}
