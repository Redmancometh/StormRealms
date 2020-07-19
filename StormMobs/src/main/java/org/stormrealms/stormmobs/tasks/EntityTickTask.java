package org.stormrealms.stormmobs.tasks;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.stormrealms.stormmobs.entity.CustomEntity;

public class EntityTickTask extends BukkitRunnable {
	@Override
	public void run() {
		Bukkit.getWorlds().forEach((world) -> {
			world.getEntities().stream().filter(entity -> ((CraftEntity) entity).getHandle() instanceof CustomEntity)
					.forEach((entity) -> ((CustomEntity) ((CraftEntity) entity).getHandle()).tick());
		});
	}
}
