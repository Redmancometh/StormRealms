package org.stormrealms.stormscript.engine;

import static org.stormrealms.stormcore.util.Fn.discardResult;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import com.google.gson.GsonBuilder;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcore.config.FileWatcher;
import org.stormrealms.stormcore.util.Console;
import org.stormrealms.stormcore.util.Either;
import org.stormrealms.stormcore.util.IterableM;
import org.stormrealms.stormcore.util.Tuple;
import org.stormrealms.stormscript.commands.StormCommand;
import org.stormrealms.stormscript.configuration.PathTypeAdapter;
import org.stormrealms.stormscript.configuration.ScriptableObjectConfig;
import org.stormrealms.stormscript.configuration.ScriptableObjectsConfig;
import org.stormrealms.stormscript.scriptable.Scriptable;

import lombok.Getter;

/**
 * Represents a component that loads and reloads scripts.
 */
@Component
public class ScriptLoader {
	private final Engine scriptEngine = Engine.create();
	private final Context.Builder defaultContextBuilder = Context.newBuilder("js")
		.allowHostAccess(HostAccess.ALL)
		.allowIO(true)
		.allowHostClassLookup(className -> true)
		.engine(scriptEngine);
	
	@Autowired
	private Console con;
	@Autowired
	private PathTypeAdapter pathTypeAdapter;
	@Autowired
	private AutowireCapableBeanFactory beanFactory;
	@Getter
	private GsonBuilder gsonBuilder;

	@PostConstruct
	public void init() {
		gsonBuilder = new GsonBuilder().registerTypeAdapter(Path.class, pathTypeAdapter);
	}

	private IterableM<Scriptable> reloadScriptableObjects(
		ScriptableObjectConfig[] objects,
		BiConsumer<Script, ScriptableObjectConfig> onChange)
	{
		return IterableM.of(objects).fmap(object -> {
			var absoluteScriptPath = Path.of("scripts").resolve(object.getScript()).toAbsolutePath();
			var script = new FileSystemScript(absoluteScriptPath, defaultContextBuilder);
			onChange.accept(script, object);

			var watcher = new FileWatcher(file -> {
				onChange.accept(script, object);
			}, absoluteScriptPath.toFile());

			watcher.start();

			return Tuple.of(object, script);
		}).fmap(
			t
			-> t.match((object, script)
			-> t.and(Either.leftOrCatch(()
			-> {
			var inst = beanFactory.createBean(object.getPrototype());

			// TODO(Yevano): Right now we're just passing strings as arguments to the
			// object, but it would be nice to pass in more structured data.
			inst.init(script, object.getArguments());
			return inst;
		})))).bind(
			t
			-> t.match((object, script, inst)
			-> inst.match(IterableM::of, err
			-> {
			con.format("Could not instantiate scriptable object class %. Error: %")
				.arg(object.getPrototype().getName())
				.arg(err);

			err.printStackTrace();
			return IterableM.of();
		})));
	}

	public IterableM<Scriptable> loadScriptableObjects(
		Path objectsConfigPath,
		BiConsumer<Script, ScriptableObjectConfig> onChange)
	{
		// NOTE(Yevano): We have to use a relative path, because ConfigManager does not
		// support absolute paths. Consider changing ConfigManager's behavior in this
		// regard.
		var fileName = Path.of("config").toAbsolutePath().relativize(objectsConfigPath.toAbsolutePath()).toString();

		var objectsConfigManager = new ConfigManager<ScriptableObjectsConfig>(
			fileName,
			ScriptableObjectsConfig.class,
			null,
			gsonBuilder);
		
		objectsConfigManager.init();

		Supplier<IterableM<Scriptable>> reload = () ->
			reloadScriptableObjects(objectsConfigManager.getConfig().getObjects(), onChange);

		objectsConfigManager.setOnReload(discardResult(reload));
		return reload.get();
	}
}