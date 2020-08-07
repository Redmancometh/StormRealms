package org.stormrealms.stormmobs.tasks;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.RPGEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class EntityTickTask extends BukkitRunnable {
	@Override
	public void run() {
		Bukkit.getWorlds().forEach((world) -> {
			world.getEntities().stream().filter(entity -> ((CraftEntity) entity).getHandle() instanceof RPGEntity)
					.forEach((entity) -> ((RPGEntity) ((CraftEntity) entity).getHandle()).tickSecond());
		});
	}
}
