package org.stormrealms.stormresources.configuration;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.stormrealms.stormcore.util.RateLimiter;

@Configuration
@ComponentScan(basePackages = { "org.stormrealms.stormresources.*", "org.stormrealms.stormresources.configuration",
		"org.stormrealms.stormresources.controller", "org.stormrealms.stormresources.listeners" })
public class ResourcesContextConfiguration {
	@Bean("gather-limiter")
	public RateLimiter limiter() {
		return new RateLimiter(TimeUnit.SECONDS, 6);
	}
}
