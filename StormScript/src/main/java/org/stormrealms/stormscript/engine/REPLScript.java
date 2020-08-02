package org.stormrealms.stormscript.engine;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.stormrealms.stormcore.util.Either;

// TODO(Yevano): REPL for server console. Maybe in-game as well?

public class REPLScript implements Script {
	@Override
	public Either<Value, Throwable> execute() {
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
