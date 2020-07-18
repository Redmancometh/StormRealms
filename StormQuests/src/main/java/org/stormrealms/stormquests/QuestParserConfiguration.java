package org.stormrealms.stormquests;

import java.lang.reflect.Modifier;

import org.bukkit.Material;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.stormrealms.stormcore.config.ConfigManager.ClassAdapter;
import org.stormrealms.stormcore.config.ConfigManager.MaterialAdapter;
import org.stormrealms.stormcore.config.ConfigManager.PathAdapter;
import org.stormrealms.stormcore.util.RuntimeTypeAdapterFactory;
import org.stormrealms.stormquests.pojo.GatherObjective;
import org.stormrealms.stormquests.pojo.InteractStartType;
import org.stormrealms.stormquests.pojo.InteractWithObjective;
import org.stormrealms.stormquests.pojo.KillObjective;
import org.stormrealms.stormquests.pojo.KillStartType;
import org.stormrealms.stormquests.pojo.QuestObjective;
import org.stormrealms.stormquests.pojo.QuestStart;
import org.stormrealms.stormquests.pojo.TalkToObjective;
import org.stormrealms.stormquests.pojo.TalkToStartType;
import org.stormrealms.stormquests.pojo.UseItemStartType;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Configuration
public class QuestParserConfiguration {
	@Bean(name = "quest-parser")
	public Gson parser(@Qualifier("objective-factory") RuntimeTypeAdapterFactory<QuestObjective> objectiveFactory,
			@Qualifier("start-factory") RuntimeTypeAdapterFactory<QuestStart> startFactory) {
		Gson newGson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.PROTECTED)
				.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
				.registerTypeHierarchyAdapter(String.class, new PathAdapter())
				.registerTypeHierarchyAdapter(Material.class, new MaterialAdapter())
				.registerTypeHierarchyAdapter(Class.class, new ClassAdapter())
				.registerTypeAdapterFactory(objectiveFactory).registerTypeAdapterFactory(startFactory)
				.setPrettyPrinting().create();
		return newGson;
	}

	@Bean(name = "start-factory")
	public RuntimeTypeAdapterFactory<QuestStart> startFactory() {
		RuntimeTypeAdapterFactory<QuestStart> stepFactory = RuntimeTypeAdapterFactory.of(QuestStart.class)
				.registerSubtype(KillStartType.class).registerSubtype(InteractStartType.class)
				.registerSubtype(TalkToStartType.class).registerSubtype(UseItemStartType.class);
		return stepFactory;

	}

	@Bean(name = "objective-factory")
	public RuntimeTypeAdapterFactory<QuestObjective> objectiveFactory() {
		RuntimeTypeAdapterFactory<QuestObjective> stepFactory = RuntimeTypeAdapterFactory.of(QuestObjective.class)
				.registerSubtype(KillObjective.class).registerSubtype(InteractWithObjective.class)
				.registerSubtype(GatherObjective.class).registerSubtype(TalkToObjective.class);
		return stepFactory;

	}

}
