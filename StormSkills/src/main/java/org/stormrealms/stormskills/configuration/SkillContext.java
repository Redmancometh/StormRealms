package org.stormrealms.stormskills.configuration;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.stormrealms.stormcore.util.RateLimiter;

@Configuration
@ComponentScan(basePackages = { "org.stormrealms.stormskills.*", "org.stormrealms.stormskills.configuration",
		"org.stormrealms.stormskills.controller", "org.stormrealms.stormskills.listeners" })
public class SkillContext {
	@Bean("shop-click-limiter")
	public RateLimiter limiter() {
		return new RateLimiter(TimeUnit.MILLISECONDS, 200);
	}
}
