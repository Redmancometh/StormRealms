package org.stormrealms.stormscript.engine;

import org.graalvm.polyglot.Context;

// TODO(Yevano): REPL for server console. Maybe in-game as well?

public class REPLScript implements Script {
	@Override
	public ScriptExecutionResult execute() {
		return null;
	}

	@Override
	public Context getContext() {
		return null;
	}
	
	@Override
	public void open() {
	}

	@Override
	public void close() {
	}
}
