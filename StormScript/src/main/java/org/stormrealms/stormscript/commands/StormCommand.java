package org.stormrealms.stormscript.commands;

import java.util.function.BiConsumer;

import org.bukkit.entity.Player;
import org.graalvm.polyglot.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.command.StormCommandHandler;
import org.stormrealms.stormcore.util.Maybe;
import org.stormrealms.stormscript.engine.Script;
import org.stormrealms.stormscript.scriptable.Scriptable;

@Component
public class StormCommand implements Scriptable {
	@Autowired
	private StormCommandHandler stormCommandHandler;

	private Script script;

	@Override
	public void init(Script script, String... args) {
		if(args.length != 1) {
			throw new RuntimeException("Command object expects one argument.");
		}

		String commandName = args[0];
		this.script = script;

		var runValue = Maybe.notNull(() -> script.getGlobalObject().getMember("run"));

		var runFunction = runValue.filter(Value::canExecute);

		// If run member exists, then our callback calls it. Otherwise, register an empty callback.
		BiConsumer<Player, String[]> callback = runFunction.match(
			(Value m) -> m::execute,
			() -> (p, c) -> { });

		stormCommandHandler.registerCommand(commandName, callback);
	}

	@Override
	public void deinit() {
		
	}

	@Override
	public Script getScript() {
		return script;
	}
}