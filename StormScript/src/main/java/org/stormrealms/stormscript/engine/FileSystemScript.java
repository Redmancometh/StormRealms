package org.stormrealms.stormscript.engine;

import java.io.IOException;
import java.nio.file.Path;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

public class FileSystemScript implements Script {
	private boolean ready = false;
	private Context.Builder contextBuilder;
	private Source source;
	private Context context;

	private Path path;

	/**
	 * Create a new script from the specified path and context builder.
	 * 
	 * @param path
	 *                           The Path that this script will be loaded from.
	 * @param contextBuilder
	 *                           The Context.Builder that will be used to build the
	 *                           Context for this script. Calling
	 *                           {@link Context.Builder#currentWorkingDirectory(Path)}
	 *                           is unnecessary, as this constructor will do that by
	 *                           default.
	 */
	public FileSystemScript(Path path, Context.Builder contextBuilder) {
		this.path = path;
		try {
			source = Source.newBuilder("js", path.toUri().toURL()).cached(false).build();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.contextBuilder = contextBuilder.currentWorkingDirectory(path.getParent().toAbsolutePath());
	}

	/**
	 * Create the {@link Context} and {@link Source} for this script. In effect,
	 * this method creates a new execution environment for this script, and loads
	 * its source code from disk. This method may be called at any time, including
	 * before calling {@link #close()}.
	 */
	@Override
	public void open() {
		context = contextBuilder.build();

		try {
			source = Source.newBuilder("js", path.toUri().toURL()).cached(false).build();
			System.out.printf("Source code:\n%s\n", source.getCharacters().toString());
			ready = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * If this script is open, execute it and return the result.
	 * 
	 * @return a {@link ScriptExecutionResult} representing the result of this
	 *         script's execution.
	 */
	@Override
	public ScriptExecutionResult execute() {
		assert ready;

		try {
			return new ScriptExecutionResult(context.eval(source));
		} catch (Throwable t) {
			return new ScriptExecutionResult(t);
		}
	}

	/**
	 * If this script is open, get the script {@link Context}.
	 * 
	 * @return this script's {@link Context}.
	 */
	@Override
	public Context getContext() {
		assert ready;
		return context;
	}

	/**
	 * If this script is open, return the result of calling {@link Source#getName()}
	 * on this script's source object.
	 * 
	 * @return the name of this script.
	 */
	@Override
	public String getName() {
		assert ready;
		return source.getName();
	}

	/**
	 * If this script is open, return the result of call {@link Source#getPath()} on
	 * this script's source object. Otherwise, the return value indicates that the
	 * script is closed.
	 */
	@Override
	public String toString() {
		return ready ? source.getPath() : "<closed script>";
	}

	/**
	 * Close the {@link Context} of this script. In effect, this method releases
	 * this script's underlying resources and stops the execution, if any, of code
	 * bound to this script.
	 */
	@Override
	public void close() {
		assert ready;
		ready = false;
		context.close(true);
	}
}
