package org.stormrealms.stormscript.configuration;

import java.nio.file.Path;

import lombok.Getter;

/**
 * POJO for (global) script configuration.
 */
public class ScriptsConfig {
	/**
	 * The base path for scripts to be automatically loaded from the file system.
	 */
	@Getter private Path scriptsBasePath;

	/**
	 * The base path for scriptable object declarations relative to the config folder.
	 */
	@Getter private Path objectsBasePath;

	@Getter private String[] autoImports;
}