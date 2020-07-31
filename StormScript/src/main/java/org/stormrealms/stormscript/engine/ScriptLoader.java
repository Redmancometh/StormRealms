package org.stormrealms.stormscript.engine;

import java.nio.file.Path;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import com.google.gson.GsonBuilder;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.config.ConfigManager;
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
	@Getter
	private ConfigManager<ScriptsConfig> scriptsConfig;
	private final Engine scriptEngine = Engine.create();
	private final Context.Builder defaultContextBuilder = Context.newBuilder("js").allowHostAccess(HostAccess.ALL)
			.allowIO(true).allowHostClassLookup(className -> true).engine(scriptEngine);

	@PostConstruct
	public void loadScriptsConfig() {
	}

	public Script loadScript(Path path, Consumer<Script> onChange) {
		var script = new FileSystemScript(path, defaultContextBuilder);

		onChange.accept(script);

		var watcher = new FileWatcher(file -> {
			onChange.accept(script);
		}, path.toFile());

		watcher.start();

		return script;
	}
}