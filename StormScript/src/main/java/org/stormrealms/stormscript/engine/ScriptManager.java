package org.stormrealms.stormscript.engine;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

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
		loadedScripts.stream().map(script -> Pair.of(script, script.execute()))
				.zipForEach((script, result) -> result.get().ifPresentOrElse(scriptReturnValue -> {
					// TODO(Yevano): Do something useful with the script return value.
					System.out.printf("Script %s was initialized successfully.", script);
				}, () -> {
					System.out.printf("Script %s failed to initialize properly. Error: ", script,
							result.getExecutionError());
				}));
	}

	public void stopAndUnloadAll() {
		loadedScripts.stream().forEach(Script::close);
	}
}