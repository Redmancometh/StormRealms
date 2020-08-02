package org.stormrealms.stormscript;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.StormSpringPlugin;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormscript.configuration.ScriptsConfig;
import org.stormrealms.stormscript.engine.ScriptManager;

import lombok.Getter;

@Component
public class StormScript extends StormSpringPlugin {
	@Autowired private ScriptManager scriptManager;

	@Getter
	private ConfigManager<ScriptsConfig> scriptsConfigManager = new ConfigManager<>("scripts/scripts.json", ScriptsConfig.class);

	@PostConstruct
	public void enable() {
		scriptsConfigManager.init();
	}

	@PreDestroy
	public void disable() {
		scriptManager.stopAndUnloadAll();
	}

	@Override
	public Class<?> getConfigurationClass() {
		return null;
	}

	@Override
	public String[] getPackages() {
		return new String[] { "org.stormrealms.stormscript", "org.stormrealms.stormscript.engine", "org.stormrealms.stormscript.configuration" };
	}

	@Override
	public void setContext(AnnotationConfigApplicationContext context) {
		super.context = context;
	}

	@Override
	public ConfigurableApplicationContext getContext() {
		return super.context;
	}
	
}