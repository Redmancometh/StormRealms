package org.stormrealms.stormscript.engine;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.config.FileWatcher;
import org.stormrealms.stormscript.configuration.PathTypeAdapter;
import org.stormrealms.stormscript.configuration.ScriptsConfig;

import lombok.Getter;

/**
 * Represents a component that loads scripts and the scripting system's
 * configuration.
 * 
 * @see ScriptsConfig
 */
@Component
public class ScriptLoader {
	@Autowired
	private PathTypeAdapter pathTypeAdapter;
	private final Path scriptsConfigPath = Path.of("config/scripts/scripts.json");
	@Getter private ScriptsConfig scriptsConfig;
	private final Engine scriptEngine = Engine.create();
	private final Context.Builder defaultContextBuilder = Context.newBuilder("js")
			.allowHostAccess(HostAccess.ALL)
			.allowIO(true)
			.allowHostClassLookup(className -> true)
			.engine(scriptEngine);

	@PostConstruct
	public void loadScriptsConfig() {
		var gson = new GsonBuilder().registerTypeAdapter(Path.class, pathTypeAdapter).create();

		try {
			var configReader = new FileReader(scriptsConfigPath.toString());
			scriptsConfig = gson.fromJson(configReader, ScriptsConfig.class);
		} catch (JsonSyntaxException e) {
			System.err.printf("Scripts config could not be parsed. Error: %s", e.getMessage());
		} catch (JsonIOException e) {
			System.err.printf("Scripts config encountered an IO error while parsing. Error: %s", e.getMessage());
		} catch (FileNotFoundException e) {
			System.err.printf("Scripts config file expected at %s, but was not found.",
					scriptsConfigPath.toAbsolutePath());
		}
	}

	private Script loadScript(Path path, Consumer<Script> onChange) {
		var script = new FileSystemScript(path, defaultContextBuilder);

		onChange.accept(script);

		var watcher = new FileWatcher(file -> {
			onChange.accept(script);
		}, path.toFile());

		watcher.start();

		return script;
	}

	public List<Script> loadAllFromConfig(Consumer<Script> onChange) {
		if (scriptsConfig == null) {
			System.out.printf("Cannot load scripts because of an invalid scripts configuration.");
			return List.of();
		}

		Stream<Path> scriptWalker;

		try {
			scriptWalker = Files.walk(scriptsConfig.getScriptsBasePath());

			var scriptList = scriptWalker
					.filter(path -> !Files.isDirectory(path))
					.map(path -> loadScript(path, onChange))
					.collect(Collectors.toList());
			
			scriptWalker.close();
			return scriptList;
		} catch (IOException e) {
			System.out.printf("Could not access scripts base path at %s.", scriptsConfig.getScriptsBasePath());
		}

		return List.of();
	}
}