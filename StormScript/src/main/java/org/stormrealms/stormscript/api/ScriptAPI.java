package org.stormrealms.stormscript.api;

import org.stormrealms.stormscript.engine.Script;

import lombok.Getter;
import lombok.NonNull;

public abstract class ScriptAPI {
	@Getter
	@NonNull
	protected Script script;

	protected ScriptAPI(Script script) {
		this.script = script;
	}
}
