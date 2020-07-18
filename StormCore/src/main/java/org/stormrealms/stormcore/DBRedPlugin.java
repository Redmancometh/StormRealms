package org.stormrealms.stormcore;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;
import org.json.simple.JSONObject;

public interface DBRedPlugin extends RedPlugin {
	@PostConstruct
	default void initialize() {
		System.out.println("NEW INITIALIZE");
		SessionFactory factory = buildSessionFactory();
		setInternalFactory(factory);
		getMappedClasses().forEach(
				(mappingClass) -> StormCore.getInstance().getMasterDB().registerDatabase(mappingClass, factory));
	}

	void setInternalFactory(SessionFactory factory);

	SessionFactory getInternalFactory();

	List<Class> getMappedClasses();

	@SuppressWarnings("deprecation")
	default SessionFactory buildSessionFactory() {
		System.out.println("NEW FACTORY");
		JSONObject jsonConfig = getConfiguration();
		JSONObject dbConfig = (JSONObject) jsonConfig.get("DB");
		Configuration configuration = new Configuration();
		Properties settings = new Properties();
		List<ClassLoader> loaders = new ArrayList();
		loaders.add(StormCore.getInstance().getPLClassLoader());
		settings.put(Environment.TC_CLASSLOADER, StormCore.getInstance().getPLClassLoader());
		settings.put(Environment.APP_CLASSLOADER, StormCore.getInstance().getPLClassLoader());
		settings.put(Environment.ENVIRONMENT_CLASSLOADER, StormCore.getInstance().getPLClassLoader());
		settings.put(Environment.HIBERNATE_CLASSLOADER, StormCore.getInstance().getPLClassLoader());
		settings.put(Environment.RESOURCES_CLASSLOADER, StormCore.getInstance().getPLClassLoader());
		settings.put(Environment.CLASSLOADERS, loaders);
		settings.put(Environment.URL, dbConfig.get("url"));
		settings.put(Environment.USER, dbConfig.get("user").toString());
		settings.put(Environment.PASS, dbConfig.get("password").toString());
		settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQL5Dialect");
		settings.put(Environment.SHOW_SQL, "false");
		settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
		settings.put(Environment.AUTOCOMMIT, "true");
		settings.put(Environment.CONNECTION_PROVIDER, "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
		settings.put(Environment.HBM2DDL_AUTO, "update");
		getMappedClasses().forEach((mappedClass) -> configuration.addAnnotatedClass(mappedClass));
		configuration.setProperties(settings);
		Thread.currentThread().setContextClassLoader(StormCore.getInstance().getPLClassLoader());
		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
				.applySettings(configuration.getProperties()).build();
		SessionFactory factory = configuration.buildSessionFactory(serviceRegistry);
		setInternalFactory(factory);
		return factory;
	}

	default void saveDefaultConfigFromJar() {

	}

	default SessionFactory getSessionFactory() {
		if (getInternalFactory() == null) {
			SessionFactory factory = buildSessionFactory();
			setInternalFactory(factory);
			return factory;
		}
		return getInternalFactory();
	}

}
