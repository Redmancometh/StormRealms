package org.stormrealms.stormscript.configuration;

import java.nio.file.Path;

import lombok.Getter;

public class ScriptsConfig {
	/**
	 * The base path for scripts to be automatically loaded from the file system.
	 */
	@Getter private Path scriptsBasePath;
}