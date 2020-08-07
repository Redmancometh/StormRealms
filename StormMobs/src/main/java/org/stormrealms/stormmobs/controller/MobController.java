package org.stormrealms.stormmobs.controller;

import javax.annotation.PostConstruct;

import org.bukkit.Bukkit;
import org.bukkit.entity.RPGEntity;
import org.springframework.stereotype.Controller;
import org.stormrealms.stormcore.StormCore;

@Controller
public class MobController {
	@PostConstruct
	public void scheduleTask() {
		System.out.println("SCHEDULED");
		Bukkit.getScheduler().runTaskTimer(StormCore.getInstance(), () -> {
			Bukkit.getWorlds().forEach((world) -> {
				world.getEntities().stream().filter(entity -> entity instanceof RPGEntity)
						.forEach((entity) -> ((RPGEntity) entity).tickSecond());
			});
		}, 20, 20);
	}
}
