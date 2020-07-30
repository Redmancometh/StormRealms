package org.stormrealms.stormmobs.tasks;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.stormrealms.stormmobs.entity.RPGEntity;

public class EntityTickTask extends BukkitRunnable {
	@Override
	public void run() {
		Bukkit.getWorlds().forEach((world) -> {
			world.getEntities().stream().filter(entity -> ((CraftEntity) entity).getHandle() instanceof RPGEntity)
					.forEach((entity) -> ((RPGEntity) ((CraftEntity) entity).getHandle()).tick());
		});
	}
}
