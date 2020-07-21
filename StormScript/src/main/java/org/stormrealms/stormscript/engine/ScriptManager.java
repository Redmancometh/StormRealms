package org.stormrealms.stormscript.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.graalvm.polyglot.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.util.StreamExtensions;

import lombok.experimental.ExtensionMethod;

@Component
@ExtensionMethod(StreamExtensions.class)
public class ScriptManager {
	@Autowired
	private ScriptLoader scriptLoader;
	private List<Script> loadedScripts = new ArrayList<>();

	public void loadAllAndExecute() {
		loadedScripts.addAll(scriptLoader.loadAllFromConfig());

		for(var script : loadedScripts) {
			var globals = script.getGlobalObject();
			globals.putMember("println", (Consumer<Object>) System.out::println);
		}

		// TODO(Yevano): Do something useful with the script return value.
		loadedScripts.stream()
			.map(script -> Pair.of(script, script.execute()))
				.zipForEach((script, result) -> result.get().ifPresentOrElse(scriptReturnValue -> {
					System.out.printf("Script %s was initialized successfully.", script);
				}, () -> {
					System.out.printf("Script %s failed to initialize properly. Error: ", script,
							result.getExecutionError());
					result.getExecutionError().printStackTrace();
				}));
	}

	public void stopAndUnloadAll() {
		loadedScripts.stream().forEach(Script::close);
	}
}