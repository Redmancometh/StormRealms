package org.stormrealms.stormscript.api;

import org.stormrealms.stormscript.engine.Script;

public class ScriptImportException extends ScriptException {
	private static final long serialVersionUID = 2268537534331335261L;
	
	public ScriptImportException(Script script, String importName) {
		super(script, String.format("Could not import %s.", importName));
	}
}