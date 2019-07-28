package org.stormrealms.stormstats.configuration;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormstats.model.RPGPlayer;

@Configuration
@ComponentScan(basePackages = { "org.stormrealms.stormstats", "org.stormrealms.stormstats.listeners",
		"org.stormrealms.stormstats.controllers" })
@EnableJpaRepositories(basePackages = { "org.stormrealms.stormstats.data" })
@EntityScan(basePackages = { "org.stormrealms.stormstats.model" })
@EnableAutoConfiguration
public class StormStatConfiguration {
	@Bean(name = "player-cache")
	public Map<UUID, RPGPlayer> playerCache() {
		return new ConcurrentHashMap<UUID, RPGPlayer>();
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		DataSource dataSource = StormCore.getInstance().getContext().getAutowireCapableBeanFactory()
				.getBean(DataSource.class);
		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		Properties properties = new Properties();
		properties.setProperty("hibernate.hbm2ddl.auto", "update");
		factory.setJpaProperties(properties);
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		factory.setDataSource(dataSource);
		factory.setJpaVendorAdapter(vendorAdapter);
		factory.setPackagesToScan("org.stormrealms.stormstats.model"); // This one
		// factory.afterPropertiesSet();
		factory.setLoadTimeWeaver(new InstrumentationLoadTimeWeaver());
		return factory;
	}

}
