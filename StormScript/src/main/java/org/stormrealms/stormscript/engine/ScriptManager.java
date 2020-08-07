package org.stormrealms.stormscript.engine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import com.google.gson.GsonBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcore.util.Console;
import org.stormrealms.stormcore.util.Either;
import org.stormrealms.stormcore.util.IterableM;
import org.stormrealms.stormscript.api.APIManager;
import org.stormrealms.stormscript.api.ImportAPI;
import org.stormrealms.stormscript.configuration.PathTypeAdapter;
import org.stormrealms.stormscript.configuration.ScriptableObjectConfig;
import org.stormrealms.stormscript.configuration.ScriptsConfig;
import org.stormrealms.stormscript.scriptable.Scriptable;

import lombok.Getter;

/**
 * Component for accessing and storing state about scripts.
 */
@Component
public class ScriptManager {
	@Autowired
	private ScriptLoader scriptLoader;
	@Autowired
	private APIManager apiManager;
	@Autowired
	private Console con;
	@Autowired
	private PathTypeAdapter pathTypeAdapter;
	@Getter
	private ConfigManager<ScriptsConfig> scriptsConfigManager;

	private List<Scriptable> loadedScriptObjects = new ArrayList<>();
	private List<Class<? extends Scriptable>> scriptablePrototypes = new ArrayList<>();

	private void setupContext(Script script) {
		var globals = script.getGlobalObject();

		for(var className : scriptsConfigManager.getConfig().getAutoImports()) {
			Class<?> autoClass = null;

			try {
				autoClass = Class.forName(className);
				System.out.printf("Class: %s\n", className);
				globals.putMember(autoClass.getSimpleName(), script.getContext().asValue(autoClass));
			} catch(ClassNotFoundException e) {
				System.out.printf("WARNING: Class %s referenced in autoImports could not be found.\n", className);
			}
		}

		var importAPI = new ImportAPI(script);
		apiManager.bindAPI(importAPI, script);
	}

	private void onLoad(Script reloadedScript, ScriptableObjectConfig object) {
		reloadedScript.open();
		setupContext(reloadedScript);

		reloadedScript.execute().match(
			returnValue -> System.out.printf(
				"Script %s was loaded successfully.\n",
				reloadedScript),

			err -> {
				System.out.printf(
					"Script %s failed to initialize properly. Error: %s\n",
					reloadedScript, err);

				err.printStackTrace();
			});
	}

	private void iterateScriptObjects(Stream<Path> stream) {
		var objects = IterableM.of(stream.iterator())
			.filter(path -> !path.toFile().isDirectory())
			.flatMap((Path path) -> scriptLoader.loadScriptableObjects(path, this::onLoad))
			.toList();

		loadedScriptObjects.addAll(objects);
	}

	@PostConstruct
	public void init() {
		scriptsConfigManager = new ConfigManager<>("scripts.json", ScriptsConfig.class, null, new GsonBuilder()
			.registerTypeAdapter(Path.class, pathTypeAdapter));
		scriptsConfigManager.init();
		var configPath = Path.of("config").toAbsolutePath();
		var objectsBasePath = configPath.resolve(scriptsConfigManager.getConfig().getObjectsBasePath());

		var walkStream = Either.leftOrCatch(() -> Files.walk(objectsBasePath.toAbsolutePath()));
		var errorString = "Could not load scripts because an IO error occurred when trying to scan the objects base path %s. Error: %s\n";

		walkStream.match(
			this::iterateScriptObjects,
			e -> System.out.printf(errorString, objectsBasePath, e));
	}

	public <T extends Scriptable> void registerPrototype(Class<T> prototypeClass) {
		scriptablePrototypes.add(prototypeClass);
		// var config = new ConfigManager<T>(String.format("config/scripts/%s",
		// configPath), class_);
	}

	/**
	 * Closes all scripts, quitting execution of any concurrently running script code.
	 */
	public void stopAndUnloadAll() {
		loadedScriptObjects.stream().forEach(Scriptable::deinit);
	}
}