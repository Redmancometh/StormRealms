package org.stormrealms.stormscript.engine;

import org.graalvm.polyglot.Context;

public interface Script extends AutoCloseable {
	public ScriptExecutionResult execute();
	public Context getContext();
	public default String getName() {
		return "<unnamed>";
	}
	public void close();
}