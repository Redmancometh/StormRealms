package org.stormrealms.stormscript;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.StormSpringPlugin;
import org.stormrealms.stormscript.configuration.StormScriptConfiguration;
import org.stormrealms.stormscript.engine.ScriptManager;

@Component
public class StormScript extends StormSpringPlugin {
	@Autowired private ScriptManager scriptManager;

	@PostConstruct
	public void enable() {
		scriptManager.loadScriptsFromConfig();
	}

	@PreDestroy
	public void disable() {
		scriptManager.unloadAllScripts();
	}

	@Override
	public Class<?> getConfigurationClass() {
		return StormScriptConfiguration.class;
	}

	@Override
	public String[] getPackages() {
		return new String[] { "org.stormrealms.stormscript", "org.stormrealms.stormscript.configuration" };
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