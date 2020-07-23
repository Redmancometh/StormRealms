package org.stormrealms.stormscript.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.util.StreamExtensions;

import lombok.experimental.ExtensionMethod;

/**
 * Component for accessing and storing state about scripts.
 */
@Component
@ExtensionMethod(StreamExtensions.class)
public class ScriptManager {
	@Autowired
	private ScriptLoader scriptLoader;
	private List<Script> loadedScripts = new ArrayList<>();

	private void setupContext(Script script) {
		var globals = script.getGlobalObject();
		globals.putMember("println", (Consumer<Object>) System.out::println);
	}

	/**
	 * Loads all scripts and enters them into reload-on-save mode.
	 */
	public void loadAllAndExecute() {
		loadedScripts.addAll(scriptLoader.loadAllFromConfig(script -> {
			script.open();
			setupContext(script);
			var result = script.execute();

			result.get().ifPresentOrElse(returnValue -> {
				System.out.printf("Script %s was loaded successfully.\n", script);
			}, () -> {
				System.out.printf("Script %s failed to initialize properly. Error: %s\n", script,
						result.getExecutionError());
				result.getExecutionError().printStackTrace();
			});
		}));
	}

	/**
	 * Closes all scripts, quitting execution of any concurrently running script code.
	 */
	public void stopAndUnloadAll() {
		loadedScripts.stream().forEach(Script::close);
	}
}