package org.stormrealms.stormresources.configuration;

import java.lang.reflect.Modifier;

import javax.annotation.PostConstruct;

import org.bukkit.Location;
import org.bukkit.Material;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcore.util.RuntimeTypeAdapterFactory;
import org.stormrealms.stormresources.configuration.pojo.HerbNode;
import org.stormrealms.stormresources.configuration.pojo.OreNode;
import org.stormrealms.stormresources.configuration.pojo.ResourceConfiguration;
import org.stormrealms.stormresources.configuration.pojo.ResourceNode;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component
public class ResourceConfigurationManager extends ConfigManager<ResourceConfiguration> {
	public ResourceConfigurationManager() {
		super("resources.json", ResourceConfiguration.class);
		RuntimeTypeAdapterFactory<ResourceNode> stepFactory = RuntimeTypeAdapterFactory.of(ResourceNode.class)
				.registerSubtype(HerbNode.class).registerSubtype(OreNode.class);
		Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.PROTECTED)
				.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
				.registerTypeHierarchyAdapter(String.class, new PathAdapter())
				.registerTypeHierarchyAdapter(Material.class, new MaterialAdapter())
				.registerTypeAdapter(Location.class, new LocationAdapter())
				.registerTypeHierarchyAdapter(Class.class, new ClassAdapter()).registerTypeAdapterFactory(stepFactory)
				.setPrettyPrinting().create();
		System.out.println("NEWGSON: " + (gson == null));
		this.gson = gson;
	}

	@PostConstruct
	public void init() {
		super.init();
	}

}
