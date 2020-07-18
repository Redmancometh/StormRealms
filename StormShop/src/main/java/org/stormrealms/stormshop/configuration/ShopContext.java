package org.stormrealms.stormshop.configuration;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.stormrealms.stormcore.util.RateLimiter;

@Configuration
@ComponentScan(basePackages = { "org.stormrealms.stormshops.*", "org.stormrealms.stormshops.configuration",
		"org.stormrealms.stormshops.controller", "org.stormrealms.stormshops.listeners" })
public class ShopContext {
	@Bean("shop-click-limiter")
	public RateLimiter limiter() {
		return new RateLimiter(TimeUnit.MILLISECONDS, 200);
	}
}
