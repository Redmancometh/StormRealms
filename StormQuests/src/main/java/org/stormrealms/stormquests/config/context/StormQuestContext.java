package org.stormrealms.stormquests.config.context;

import java.util.HashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.stormrealms.stormquests.pojo.Quest;

@Configuration
@ComponentScan(basePackages = { "org.stormrealms.stormquests", "org.stormrealms.stormquests.pojo",
		"org.stormrealms.stormquests.config.context" })
public class StormQuestContext {

	@Bean(name = "quests")
	public HashMap<Integer, Quest> quests() {
		return new HashMap();
	}

}
