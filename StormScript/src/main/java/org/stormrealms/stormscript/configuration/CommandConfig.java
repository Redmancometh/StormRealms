package org.stormrealms.stormscript.configuration;

import lombok.Getter;
import lombok.NonNull;

public class CommandConfig {
	@Getter
	@NonNull
	private String label;
	@Getter
	@NonNull
	private String description;
	@Getter
	@NonNull
	private String script;
}