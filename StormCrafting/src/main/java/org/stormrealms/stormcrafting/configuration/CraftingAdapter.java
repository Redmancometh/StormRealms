package org.stormrealms.stormcrafting.configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcrafting.configuration.pojo.BrewingIngredients;
import org.stormrealms.stormcrafting.configuration.pojo.CraftingIngredient;
import org.stormrealms.stormcrafting.configuration.pojo.GrindingIngredients;
import org.stormrealms.stormcrafting.configuration.pojo.SmithingIngredients;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class CraftingAdapter {
	private static SmithingIngredients smithingIngredients;
	private static BrewingIngredients brewingIngredients;
	private static GrindingIngredients grindingIngredients;
	private static Map<String, CraftingIngredient> ingredientMap = new HashMap();
	static {
		ConfigManager<BrewingIngredients> brewMan = new ConfigManager("brewingingredients.json",
				BrewingIngredients.class);
		brewMan.init();
		brewingIngredients = brewMan.getConfig();
		ConfigManager<GrindingIngredients> grindMan = new ConfigManager("grindingingredients.json",
				GrindingIngredients.class);
		grindMan.init();
		grindingIngredients = grindMan.getConfig();
		brewingIngredients = brewMan.getConfig();

		ConfigManager<SmithingIngredients> smithMan = new ConfigManager("smithingingredients.json",
				SmithingIngredients.class);
		smithMan.init();
		smithingIngredients = smithMan.getConfig();
		ingredientMap.putAll(brewingIngredients.getIngredients());
		System.out.println("GRINDING IS NULL: " + (grindingIngredients == null));
		ingredientMap.putAll(grindingIngredients.getIngredients());
		ingredientMap.putAll(smithingIngredients.getIngredients());
		for (int x = 0; x < 10; x++)
			System.out.println("Ingredient map size: " + ingredientMap.size());
	}

	public static class IngredientAdapter extends TypeAdapter<CraftingIngredient> {

		@Override
		public void write(JsonWriter arg0, CraftingIngredient arg1) throws IOException {
			arg0.jsonValue(arg1.toString());
		}

		@Override
		public CraftingIngredient read(JsonReader reader) throws IOException {
			System.out.println("READING");
			JsonToken token = reader.peek();
			String ingredientName = reader.nextString();
			return ingredientMap.get(ingredientName);
		}
	}
}
