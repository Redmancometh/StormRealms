package org.stormrealms.stormquests.config.context;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormquests.pojo.Quest;

@Configuration
@ComponentScan(basePackages = { "org.stormrealms.stormquests", "org.stormrealms.stormquests.pojo",
		"org.stormrealms.stormquests.config.context" })
public class StormQuestContext {
	@Bean
	public StormCore stormCore() {
		return (StormCore) Bukkit.getPluginManager().getPlugin("StormCore");
	}

	@Bean(name = "quests")
	public HashMap<Integer, Quest> quests() {
		return new HashMap();
	}

}
