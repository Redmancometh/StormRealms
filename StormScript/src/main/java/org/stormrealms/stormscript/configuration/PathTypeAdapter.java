package org.stormrealms.stormscript.configuration;

import java.lang.reflect.Type;
import java.nio.file.Path;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.springframework.stereotype.Component;

/**
 * Implements GSON (de)serialization for {@link Path}.
 */
@Component
public class PathTypeAdapter implements JsonSerializer<Path>, JsonDeserializer<Path> {
	@Override
	public Path deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		return Path.of(json.getAsString());
	}

	@Override
	public JsonElement serialize(Path src, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonPrimitive(src.toString());
	}
}