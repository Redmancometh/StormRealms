package org.stormrealms.stormscript.configuration;

import java.nio.file.Path;

import lombok.Getter;

public class ScriptableObjectConfig {
	@Getter private Class<?> prototype;
	@Getter private Path script;
}
