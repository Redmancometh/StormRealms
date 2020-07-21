package org.stormrealms.stormscript.engine;

import java.io.IOException;
import java.nio.file.Path;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

public class FileSystemScript implements Script {
	private Source source;
	private Context.Builder contextBuilder;
	private Context context;

	public FileSystemScript(Path path, Context.Builder contextBuilder) {
		try {
			source = Source.newBuilder("js", path.toUri().toURL()).build();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.contextBuilder = contextBuilder.currentWorkingDirectory(path.getParent().toAbsolutePath());
		context = contextBuilder.build();
	}

	/**
	 * Gives this script a new context.
	 */
	@Override
	public void reload() {
		context.close();
		context = contextBuilder.build();
	}

	@Override
	public ScriptExecutionResult execute() {
		try {
			return new ScriptExecutionResult(context.eval(source));
		} catch(Throwable t) {
			return new ScriptExecutionResult(t);
		}
	}

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public String getName() {
		return source.getName();
	}

	@Override
	public String toString() {
		return source.getPath();
	}

	@Override
	public void close() {
		context.close(true);
	}
}
