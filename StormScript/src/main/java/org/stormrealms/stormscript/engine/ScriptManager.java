package org.stormrealms.stormscript.engine;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
public class ScriptManager {
	@Getter private Engine scriptEngine;
	private List<Script> loadedScripts = new ArrayList<>();

	@PostConstruct
	public void init() {
		scriptEngine = Engine.create();
	}

	public FileSystemScript createScriptFromFile(Path path) {
		// TODO(Yevano): Configurable script IO
		var script = new FileSystemScript(path, Context.newBuilder("js")
			.allowHostAccess(HostAccess.ALL)
			.allowIO(true)
			.in(System.in)
			.out(System.out)
			.engine(scriptEngine));

		loadedScripts.add(script);
		return script;
	}

	public void loadScriptsFromConfig() {

	}

	public void unloadAllScripts() {
		loadedScripts.stream().forEach(Script::close);
	}
}