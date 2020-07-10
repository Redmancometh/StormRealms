package org.stormrealms.stormcore;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScriptingContext {
	@Bean
	public ScriptEngine engine() {
		return new ScriptEngineManager().getEngineByName("ecmascript");
	}
}
