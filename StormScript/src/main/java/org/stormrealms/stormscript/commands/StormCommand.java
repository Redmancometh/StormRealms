package org.stormrealms.stormscript.commands;

import org.stormrealms.stormscript.engine.Script;
import org.stormrealms.stormscript.scriptable.Scriptable;

public class StormCommand implements Scriptable {
	protected Script script;

	@Override
	public void init(Script script) {
		this.script = script;
	}

	@Override
	public void deinit() {
		
	}

	@Override
	public Script getScript() {
		return script;
	}
}