package org.stormrealms.stormstats.configuration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import org.stormrealms.stormstats.configuration.pojo.DefaultsConfig;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.redmancometh.redcore.config.ConfigManager;

public class DefaultStatConfigManager extends ConfigManager<DefaultsConfig> {

	public DefaultStatConfigManager(String configName, Class<DefaultsConfig> confClass) {
		super(configName, confClass);
	}

	private Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.PROTECTED)
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
			.registerTypeHierarchyAdapter(String.class, new PathAdapter())
			.registerTypeHierarchyAdapter(Class.class, new ClassAdapter()).setPrettyPrinting().create();

	@Override
	public Gson getGson() {
		return gson;
	}

	public static class PathAdapter extends TypeAdapter<String> {

		@Override
		public String read(JsonReader arg0) throws IOException {
			String string = arg0.nextString();
			if (string.contains("http"))
				return string;
			return string.replace("//", File.separator).replace("\\", File.separator);
		}

		@Override
		public void write(JsonWriter arg0, String arg1) throws IOException {
			arg0.value(arg1);
		}

	}
}
