package org.stormrealms.stormcore;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.stormrealms.databasing.MasterDatabase;
import org.stormrealms.stormcore.command.StormCommandHandler;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcore.config.pojo.SpringConfig;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

@ComponentScan
//TODO: DI-ify the RedCore stuff.
public class StormCore extends JavaPlugin {
	private static ConfigManager<SpringConfig> cfgMon = new ConfigManager("spring.json", SpringConfig.class);
	protected AnnotationConfigApplicationContext context;
	private ClassLoader masterLoader;
	private static StormCore instance;
	private RedPlugins getPlugins;
	private Executor pool = Executors.newFixedThreadPool(8,
			new ThreadFactoryBuilder().setNameFormat("RedCore-%d").build());
	private SessionFactory sessionFactory;
	private MasterDatabase masterDB;

	Function<Path, URL> pathMapperFunc = (p) -> {
		try {
			return p.toUri().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	};

	public void onEnable() {
		setPluginManager(new RedPlugins());
		setMasterDB(new MasterDatabase());
		instance = this;
		this.context = new AnnotationConfigApplicationContext();
		cfgMon.init();
		SpringConfig cfg = cfgMon.getConfig();
		this.masterLoader = masterLoader();
		this.context.setClassLoader(masterLoader);
		this.context.register(StormCoreConfiguration.class);
		Map<String, Object> props = context.getEnvironment().getSystemProperties();
		cfg.getProperties().forEach((key, value) -> props.put(key, value));
		context.getEnvironment().setActiveProfiles(cfg.getProfiles().toArray(new String[cfg.getProfiles().size()]));
		this.context.refresh();
		Bukkit.getPluginManager().registerEvents(context.getBean(StormCommandHandler.class), this);
		Logger.getLogger(StormCoreConfiguration.class.getName()).info("StormCore has started!");
	}

	private URLClassLoader masterLoader() {
		try (Stream<Path> pathStream = Files.walk(new File("").toPath().toAbsolutePath())) {
			List<URL> urls = pathStream.filter(path1 -> path1.toString().endsWith(".jar")).map(pathMapperFunc)
					.collect(Collectors.toList());
			return new URLClassLoader(urls.toArray(new URL[urls.size()]), this.getClassLoader());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public RedPlugins getPluginManager() {
		return getPlugins;
	}

	public void setPluginManager(RedPlugins pluginManager) {
		this.getPlugins = pluginManager;
	}

	public Executor getPool() {
		return pool;
	}

	public MasterDatabase getMasterDB() {
		return masterDB;
	}

	public void setMasterDB(MasterDatabase masterDB) {
		this.masterDB = masterDB;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public static StormCore getInstance() {
		return instance;
	}

	public AnnotationConfigApplicationContext getContext() {
		return context;
	}

	public ClassLoader getPLClassLoader() {
		return this.masterLoader;
	}
}
