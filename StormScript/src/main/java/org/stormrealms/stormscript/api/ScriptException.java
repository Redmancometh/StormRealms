package org.stormrealms.stormscript.api;

import org.stormrealms.stormscript.engine.Script;

import lombok.Getter;
import lombok.NonNull;

public abstract class ScriptException extends Exception {
	private static final long serialVersionUID = 6758864787442407089L;
	
	@NonNull
	@Getter
	protected Script script;

	protected ScriptException(Script script, String problem) {
		super(String.format("Error in script %s: %s", script.getName(), problem));
	}
}
