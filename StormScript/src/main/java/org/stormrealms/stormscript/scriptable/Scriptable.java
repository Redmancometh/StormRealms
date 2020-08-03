package org.stormrealms.stormscript.scriptable;

import org.stormrealms.stormscript.engine.Script;

public interface Scriptable {
	void init(Script script);
	void deinit();
	Script getScript();
}