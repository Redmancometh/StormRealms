package org.stormrealms.stormscript.engine;

import static org.stormrealms.stormcore.util.Fn.discardResult;

import java.io.File;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcore.config.FileWatcher;
import org.stormrealms.stormcore.util.Console;
import org.stormrealms.stormcore.util.Either;
import org.stormrealms.stormcore.util.IterableM;
import org.stormrealms.stormcore.util.Just;
import org.stormrealms.stormcore.util.None;
import org.stormrealms.stormcore.util.Tuple;
import org.stormrealms.stormscript.configuration.ScriptableObjectConfig;
import org.stormrealms.stormscript.configuration.ScriptableObjectsConfig;
import org.stormrealms.stormscript.scriptable.Scriptable;

/**
 * Represents a component that loads and reloads scripts.
 */
@Component
public class ScriptLoader {
	private final Engine scriptEngine = Engine.create();
	private final Context.Builder defaultContextBuilder = Context.newBuilder("js").allowHostAccess(HostAccess.ALL)
		.allowIO(true).allowHostClassLookup(className -> true).engine(scriptEngine);
	@Autowired
	private Console con;

	private IterableM<Scriptable> reloadScriptableObjects(ScriptableObjectConfig[] objects, BiConsumer<Script, ScriptableObjectConfig> onChange) {
		return IterableM.of(objects)
			.fmap(object -> {
				var script = new FileSystemScript(object.getScript(), defaultContextBuilder);
				onChange.accept(script, object);

				var watcher = new FileWatcher(file -> {
					onChange.accept(script, object);
				}, object.getScript().toFile());

				watcher.start();

				return Tuple.of(object, script);
			})

			.fmap(t -> t.match((object, script) -> t.and(Either.leftOrCatch(() -> {
				var inst = object.getPrototype().getConstructor().newInstance();
				inst.init(script);
				return inst;
			}))))

			.flatMap(t -> t.match((object, script, inst) -> inst.match(Just::of, err -> {
				con.format("Could not instantiate scriptable object class %. Error: %")
					.arg(object.getPrototype().getName())
					.arg(err);

				err.printStackTrace();
				return None.none();
			})));
	}

	public IterableM<Scriptable> loadScriptableObjects(Path objectsConfigPath, BiConsumer<Script, ScriptableObjectConfig> onChange) {
		// NOTE(Yevano): We have to use a relative path, because ConfigManager does not
		// support absolute paths. Consider changing ConfigManager's behavior in this
		// regard.
		con.format("% of % = %")
			.arg(Path.of("config").toAbsolutePath())
			.arg(objectsConfigPath.toAbsolutePath())
			.arg(Path.of("config").toAbsolutePath().relativize(objectsConfigPath.toAbsolutePath()))
			.out();
		System.out.println(Path.of("config").toAbsolutePath());
		System.out.println(objectsConfigPath.toAbsolutePath());
		System.out.println(Path.of("config").toAbsolutePath().relativize(objectsConfigPath.toAbsolutePath()));
		var fileName = Path.of("config").toAbsolutePath().relativize(objectsConfigPath.toAbsolutePath()).toString();
		con.out(fileName);
		var objectsConfigManager = new ConfigManager<ScriptableObjectsConfig>(fileName, ScriptableObjectsConfig.class);
		objectsConfigManager.init();
		
		Supplier<IterableM<Scriptable>> reload = () -> reloadScriptableObjects(objectsConfigManager.getConfig().getObjects(), onChange);

		objectsConfigManager.setOnReload(discardResult(reload));
		return reload.get();
	}
}