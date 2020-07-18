package org.stormrealms.stormcrafting.configuration;

import java.lang.reflect.Modifier;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.stormrealms.stormcore.config.ConfigManager.ClassAdapter;
import org.stormrealms.stormcore.config.ConfigManager.LocationAdapter;
import org.stormrealms.stormcore.config.ConfigManager.MaterialAdapter;
import org.stormrealms.stormcore.config.ConfigManager.PathAdapter;
import org.stormrealms.stormcore.config.ConfigManager.RPGStatAdapter;
import org.stormrealms.stormcore.outfacing.RPGStat;
import org.stormrealms.stormcore.util.RateLimiter;
import org.stormrealms.stormcrafting.configuration.CraftingAdapter.IngredientAdapter;
import org.stormrealms.stormcrafting.configuration.pojo.BrewingConfig;
import org.stormrealms.stormcrafting.configuration.pojo.CraftingIngredient;
import org.stormrealms.stormcrafting.configuration.pojo.GrindingConfig;
import org.stormrealms.stormcrafting.configuration.pojo.SmithingConfig;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Configuration
@ComponentScan(basePackages = { "org.stormrealms.stormresources.*", "org.stormrealms.stormresources.configuration",
		"org.stormrealms.stormresources.controller", "org.stormrealms.stormresources.listeners" })
public class CraftingContext {
	@Bean("shop-click-limiter")
	public RateLimiter limiter() {
		return new RateLimiter(TimeUnit.MILLISECONDS, 200);
	}

	@Bean("crafting-parser")
	public Gson craftingParser() {
		Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.PROTECTED)
				.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
				.registerTypeHierarchyAdapter(String.class, new PathAdapter())
				.registerTypeHierarchyAdapter(Material.class, new MaterialAdapter())
				.registerTypeAdapter(Location.class, new LocationAdapter())
				.registerTypeAdapter(RPGStat.class, new RPGStatAdapter())
				.registerTypeAdapter(CraftingIngredient.class, new IngredientAdapter())
				.registerTypeHierarchyAdapter(Class.class, new ClassAdapter()).setLenient().create();

		return gson;
	}

	@Bean
	public CraftingConfigManager<BrewingConfig> brewingMan(
			@Autowired @Qualifier("crafting-parser") Gson craftingParser) {
		CraftingConfigManager<BrewingConfig> brewMan = new CraftingConfigManager("brewing.json", BrewingConfig.class);
		brewMan.setGson(craftingParser);
		brewMan.init();
		return brewMan;
	}

	@Bean
	public CraftingConfigManager<GrindingConfig> grindingMan(
			@Autowired @Qualifier("crafting-parser") Gson craftingParser) {
		CraftingConfigManager<GrindingConfig> brewMan = new CraftingConfigManager("grinding.json",
				GrindingConfig.class);
		brewMan.setGson(craftingParser);
		brewMan.init();
		return brewMan;
	}

	@Bean
	public CraftingConfigManager<SmithingConfig> smithMan(
			@Autowired @Qualifier("crafting-parser") Gson craftingParser) {
		CraftingConfigManager<SmithingConfig> brewMan = new CraftingConfigManager("smithing.json",
				SmithingConfig.class);
		brewMan.setGson(craftingParser);
		brewMan.init();
		System.out.println("SMITHING CFG");
		System.out.println(brewMan.getConfig());

		return brewMan;
	}

}
