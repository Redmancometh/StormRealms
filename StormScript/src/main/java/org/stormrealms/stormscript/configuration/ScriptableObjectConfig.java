package org.stormrealms.stormscript.configuration;

import java.nio.file.Path;

import org.stormrealms.stormscript.scriptable.Scriptable;

import lombok.Getter;

public class ScriptableObjectConfig {
	@Getter private Class<Scriptable> prototype;
	@Getter private Path script;
}
