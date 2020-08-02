package org.stormrealms.stormscript.engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.util.Fn;
import org.stormrealms.stormcore.util.IterableM;
import org.stormrealms.stormscript.api.APIManager;
import org.stormrealms.stormscript.api.ImportAPI;
import org.stormrealms.stormscript.scriptable.Scriptable;

/**
 * Component for accessing and storing state about scripts.
 */
@Component
public class ScriptManager {
	@Autowired
	private ScriptLoader scriptLoader;
	@Autowired
	private APIManager apiManager;

	private List<Script> loadedScripts = new ArrayList<>();
	private List<Class<? extends Scriptable>> scriptablePrototypes = new ArrayList<>();

	@PostConstruct
	public void init() {
		var objectsBasePath = scriptLoader.getScriptsConfig().getConfig().getObjectsBasePath();

		Stream<Path> walkStream;

		try(Stream<Path> tryWalkStream = Files.walk(objectsBasePath)) {
			walkStream = tryWalkStream;
		} catch (IOException e) {
			System.out.printf(
					"Could not load scripts because an IO error ocurred when trying to scan the base path %s. Error: %s\n",
					objectsBasePath, e);
			return;
        }

        IterableM.of(walkStream.iterator())
			.filter(path -> !path.toFile().isDirectory())
			.fmap(Fn.unit(path -> {
				scriptLoader.loadScript(path, reloadedScript -> {
					reloadedScript.open();
					setupContext(reloadedScript);
					reloadedScript.execute().match(
						returnValue -> System.out.printf("Script %s was loaded successfully.\n", reloadedScript),
						err -> {
							System.out.printf("Script %s failed to initialize properly. Error: %s\n", reloadedScript, err);
							err.printStackTrace();
						});
				});
			}));
	}

	private void setupContext(Script script) {
		var globals = script.getGlobalObject();

		for(var className : scriptLoader.getScriptsConfig().getConfig().getAutoImports()) {
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

	public <T extends Scriptable> void registerPrototype(Class<T> prototypeClass) {
		scriptablePrototypes.add(prototypeClass);
		// var config = new ConfigManager<T>(String.format("config/scripts/%s",
		// configPath), class_);
	}

	/**
	 * Closes all scripts, quitting execution of any concurrently running script code.
	 */
	public void stopAndUnloadAll() {
		loadedScripts.stream().forEach(Script::close);
	}
}