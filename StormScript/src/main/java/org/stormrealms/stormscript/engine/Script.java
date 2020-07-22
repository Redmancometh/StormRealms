package org.stormrealms.stormscript.engine;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

public interface Script extends AutoCloseable {
	public ScriptExecutionResult execute();

	public Context getContext();

	public default Value getGlobalObject() {
		return getContext().getBindings("js");
	}

	public default String getName() {
		return "<unnamed>";
	}

	public void reload();

	public void close();
}