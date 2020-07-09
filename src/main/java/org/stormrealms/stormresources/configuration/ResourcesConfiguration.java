package org.stormrealms.stormresources.configuration;

import java.lang.reflect.Modifier;

import org.bukkit.Material;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcore.config.ConfigManager.ClassAdapter;
import org.stormrealms.stormcore.config.ConfigManager.MaterialAdapter;
import org.stormrealms.stormcore.config.ConfigManager.PathAdapter;
import org.stormrealms.stormcore.util.RuntimeTypeAdapterFactory;
import org.stormrealms.stormresources.configuration.pojo.ResourceConfiguration;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Configuration
@ComponentScan(basePackages = { "org.stormrealms.stormresources.*", "org.stormrealms.stormresources.configuration",
		"org.stormrealms.stormresources.controller", "org.stormrealms.stormresources.listeners" })
public class ResourcesConfiguration {
	@Bean(name = "resources-parser")
	public Gson parser(@Qualifier("node-factory") RuntimeTypeAdapterFactory<ResourceNode> nodeFactory) {
		Gson newGson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.PROTECTED)
				.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
				.registerTypeHierarchyAdapter(String.class, new PathAdapter())
				.registerTypeHierarchyAdapter(Material.class, new MaterialAdapter())
				.registerTypeHierarchyAdapter(Class.class, new ClassAdapter()).registerTypeAdapterFactory(nodeFactory)
				.setPrettyPrinting().create();
		return newGson;
	}

	@Bean(name = "node-factory")
	public RuntimeTypeAdapterFactory<ResourceNode> startFactory() {
		RuntimeTypeAdapterFactory<ResourceNode> stepFactory = RuntimeTypeAdapterFactory.of(ResourceNode.class)
				.registerSubtype(HerbNode.class).registerSubtype(OreNode.class);
		return stepFactory;

	}

	@Bean("resources-config")
	public ConfigManager<ResourceConfiguration> confMan() {
		ConfigManager<ResourceConfiguration> confMan = new ConfigManager("resources.json", ResourceConfiguration.class);
		confMan.init();
		return confMan;
	}

}
