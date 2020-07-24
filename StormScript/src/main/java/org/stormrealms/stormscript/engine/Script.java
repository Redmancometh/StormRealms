package org.stormrealms.stormscript.engine;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

public interface Script extends AutoCloseable {
	public ScriptExecutionResult execute();

	public Context getContext();

	/**
	 * Alias method for {@code script.getContext().getBindings("js")}.
	 * 
	 * @return the global object or "bindings" of this script. This object is
	 *         equivalent to the global scope of this script.
	 */
	public default Value getGlobalObject() {
		return getContext().getBindings("js");
	}

	public default String getName() {
		return "<unnamed>";
	}

	public void open();

	public void close();
}