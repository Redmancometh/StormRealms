package org.stormrealms.stormscript.engine;

import java.util.Optional;

import org.graalvm.polyglot.Value;

import lombok.NonNull;

public class ScriptExecutionResult {
	@NonNull
	private Optional<Value> returnValue;
	@NonNull
	private Optional<Throwable> executionError;

	/**
	 * Create a result representing a failed execution.
	 * 
	 * @param executionError
	 *                           The {@link Throwable} which represents the cause of
	 *                           the failure; preferably an exception object thrown
	 *                           by
	 *                           {@link org.graalvm.polyglot.Context#eval(org.graalvm.polyglot.Source)}.
	 */
	public ScriptExecutionResult(Throwable executionError) {
		this.returnValue = Optional.empty();
		this.executionError = Optional.of(executionError);
	}

	/**
	 * Create a result representing a successful execution.
	 * 
	 * @param returnValue
	 *                        The return value of a call to
	 *                        {@link org.graalvm.polyglot.Context#eval(org.graalvm.polyglot.Source)}.
	 */
	public ScriptExecutionResult(Value returnValue) {
		this.returnValue = Optional.of(returnValue);
	}

	/**
	 * Get the return value if the execution was successful. Rethrow on failure.
	 * @return the return value of the execution.
	 * @throws Throwable
	 */
	public Value getOrThrow() throws Throwable {
		return returnValue.orElseThrow(() -> executionError.get());
	}

	public Optional<Value> get() {
		return returnValue;
	}

	public Throwable getExecutionError() {
		return executionError.get();
	}
}