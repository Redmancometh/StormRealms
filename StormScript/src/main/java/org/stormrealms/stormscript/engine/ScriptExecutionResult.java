package org.stormrealms.stormscript.engine;

import java.util.Optional;

import org.graalvm.polyglot.Value;

public class ScriptExecutionResult {
    private Optional<Value> returnValue;
    private Throwable executionError;

    public ScriptExecutionResult(Throwable executionError) {
        this.returnValue = Optional.empty();
        this.executionError = executionError;
    }

    public ScriptExecutionResult(Value returnValue) {
        this.returnValue = Optional.of(returnValue);
    }

    public Value getOrThrow() throws Throwable {
        return returnValue.orElseThrow(() -> executionError);
    }

    public Optional<Value> get() {
        return returnValue;
    }
}