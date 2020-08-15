package org.stormrealms.stormscript.configuration;

import java.io.IOException;
import java.nio.file.Path;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.springframework.stereotype.Component;

/**
 * Implements GSON (de)serialization for {@link Path}.
 */
@Component
public class PathTypeAdapter extends TypeAdapter<Path> {
	@Override
	public Path read(JsonReader reader) throws IOException {
		return Path.of(reader.nextString());
	}

	@Override
	public void write(JsonWriter writer, Path path) throws IOException {
		writer.value(path.toString());
	}
}