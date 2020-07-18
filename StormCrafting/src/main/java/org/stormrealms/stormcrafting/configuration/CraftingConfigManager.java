package org.stormrealms.stormcrafting.configuration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcore.outfacing.RPGStat;
import org.stormrealms.stormcrafting.configuration.CraftingAdapter.IngredientAdapter;
import org.stormrealms.stormcrafting.configuration.pojo.CraftingIngredient;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;

public class CraftingConfigManager<T> extends ConfigManager<T> {

	public CraftingConfigManager(String fileName, Class clazz) {
		super(fileName, clazz);
		this.gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.PROTECTED)
				.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
				.registerTypeHierarchyAdapter(String.class, new PathAdapter())
				.registerTypeHierarchyAdapter(Material.class, new MaterialAdapter())
				.registerTypeAdapter(Location.class, new LocationAdapter())
				.registerTypeAdapter(RPGStat.class, new RPGStatAdapter())
				.registerTypeAdapter(CraftingIngredient.class, new IngredientAdapter())
				.registerTypeHierarchyAdapter(Class.class, new ClassAdapter()).setLenient().create();
	}

	public void init() {
		initConfig();
		super.registerMonitor();
	}

	protected void initConfig() {
		for (int x = 0; x < 2; x++)
			Logger.getLogger("Test").log(Level.SEVERE, "Loading file: " + fileName);
		try (FileReader reader = new FileReader("config" + File.separator + fileName)) {
			T conf = (T) this.gson.fromJson(reader, clazz);
			this.config = conf;
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}
}
