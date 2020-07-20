package org.stormrealms.stormmobs.controller;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.bukkit.entity.EntityType;
import org.springframework.stereotype.Controller;
import org.stormrealms.stormcore.util.ReflectionUtil;
import org.stormrealms.stormmobs.entity.mixin.CustomZombie;

import net.minecraft.server.EntityTypes;

@Controller
public class MobController {

	@PostConstruct
	public void registerMobs() {
		register("customzombie", EntityType.ZOMBIE.getTypeId(), CustomZombie.class);
	}

	public static void register(String name, int id, Class<?> registryClass) {
		((Map) ReflectionUtil.getPrivateField("c", EntityTypes.class, null)).put(name, registryClass);
		((Map) ReflectionUtil.getPrivateField("d", EntityTypes.class, null)).put(registryClass, name);
		((Map) ReflectionUtil.getPrivateField("f", EntityTypes.class, null)).put(registryClass, id);
	}

}
