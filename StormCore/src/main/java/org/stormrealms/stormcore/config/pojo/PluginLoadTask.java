package org.stormrealms.stormcore.config.pojo;

import org.stormrealms.stormcore.util.PluginConfig;

import lombok.Getter;
import lombok.Setter;

public class PluginLoadTask {
	@Getter
	@Setter
	PluginConfig config;

	public PluginLoadTask(PluginConfig config2) {
		this.config = config2;
	}
}
