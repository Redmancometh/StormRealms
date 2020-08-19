package org.stormrealms.stormscript.engine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcore.util.Console;
import org.stormrealms.stormcore.util.Either;
import org.stormrealms.stormcore.util.IterableM;
import org.stormrealms.stormscript.api.APIManager;
import org.stormrealms.stormscript.api.ImportAPI;
import org.stormrealms.stormscript.configuration.ScriptableObjectConfig;
import org.stormrealms.stormscript.configuration.ScriptsConfig;
import org.stormrealms.stormscript.proxy.ClassProxy;
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
	@Getter
	private ConfigManager<ScriptsConfig> scriptsConfigManager;

	private List<Scriptable> loadedScriptObjects = new ArrayList<>();
	private List<Class<? extends Scriptable>> scriptablePrototypes = new ArrayList<>();

	private void setupContext(Script script) {
		var globals = script.getGlobalObject();

		for (var className : scriptsConfigManager.getConfig().getAutoImports()) {
			Class<?> autoClass = null;

			try {
				autoClass = Class.forName(className);
				var classProxy = new ClassProxy<>(autoClass);
				globals.putMember(autoClass.getSimpleName(), script.getContext().asValue(classProxy));
			} catch (ClassNotFoundException e) {
				con.format("WARNING: Class % referenced in autoImports could not be found.\n")
					.arg(className)
					.out();
			}
		}

		var importAPI = new ImportAPI(script);
		apiManager.bindAPI(importAPI, script);

		globals.putMember("print", (Consumer<Object>) str -> con.out(str));
	}

	private void onLoad(Script reloadedScript, ScriptableObjectConfig object) {
		reloadedScript.open();
		setupContext(reloadedScript);

		reloadedScript.execute().match(returnValue -> {
			con.format("Script % was loaded successfully.\n")
				.arg(reloadedScript)
				.out();
		}, err -> {
			con.format("Script % failed to initialize properly. Error: %\n")
				.arg(reloadedScript)
				.arg(err)
				.out();

			err.printStackTrace();
		});
	}

	private void iterateScriptObjects(Stream<Path> stream) {
		var objects = IterableM.of(stream.iterator())
			.filter(path -> !path.toFile().isDirectory())
			.bind((Path path) -> scriptLoader.loadScriptableObjects(path, this::onLoad))
			.toList();

		loadedScriptObjects.addAll(objects);
	}

	@PostConstruct
	public void init() {
		scriptsConfigManager = new ConfigManager<>("scripts.json", ScriptsConfig.class, null, scriptLoader.getGsonBuilder());
		scriptsConfigManager.init();
		var configPath = Path.of("config").toAbsolutePath();
		var objectsBasePath = configPath.resolve(scriptsConfigManager.getConfig().getObjectsBasePath());

		var walkStream = Either.leftOrCatch(() -> Files.walk(objectsBasePath.toAbsolutePath()));
		var errorFormat = con.format("Could not load scripts because an IO error occurred when trying to scan the objects base path %. Error: %\n");

		walkStream.match(
			this::iterateScriptObjects,
			e -> errorFormat.arg(objectsBasePath).arg(e).out());
	}

	public <T extends Scriptable> void registerPrototype(Class<T> prototypeClass) {
		scriptablePrototypes.add(prototypeClass);
	}

	/**
	 * Closes all scripts, quitting execution of any concurrently running script code.
	 */
	public void stopAndUnloadAll() {
		loadedScriptObjects.stream().forEach(Scriptable::deinit);
	}
}