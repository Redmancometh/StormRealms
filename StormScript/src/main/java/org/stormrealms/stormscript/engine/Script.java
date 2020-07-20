package org.stormrealms.stormscript.engine;

import org.graalvm.polyglot.Context;

public interface Script extends AutoCloseable {
	public ScriptExecutionResult execute();
	public Context getContext();
	public void close();
}