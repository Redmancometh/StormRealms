package org.stormrealms.stormscript.engine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.util.Either;
import org.stormrealms.stormcore.util.IterableM;
import org.stormrealms.stormcore.util.SupplierThrows;
import org.stormrealms.stormscript.StormScript;
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
	private StormScript module;
	@Autowired
	private APIManager apiManager;

	private List<Script> loadedScripts = new ArrayList<>();
	private List<Class<? extends Scriptable>> scriptablePrototypes = new ArrayList<>();

	private void setupContext(Script script) {
		var globals = script.getGlobalObject();

		for(var className : module.getScriptsConfigManager().getConfig().getAutoImports()) {
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

	private void onLoad(Script reloadedScript) {
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

	private void iterateScripts(Stream<Path> stream) {
		IterableM.of(stream.iterator())
			.filter(path -> !path.toFile().isDirectory())
			.fmap(path -> loadedScripts.add(scriptLoader.loadScript(path, this::onLoad)));
	}

	@PostConstruct
	public void init() {
		var objectsBasePath = module.getScriptsConfigManager().getConfig().getObjectsBasePath();

		var walkStream = Either.leftOrCatch((SupplierThrows<Stream<Path>, Throwable>) () -> Files.walk(objectsBasePath));
		var errorString = "Could not load scripts because an IO error ocurred when trying to scan the base path %s. Error: %s\n";

		walkStream.match(
			this::iterateScripts,
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
		loadedScripts.stream().forEach(Script::close);
	}
}