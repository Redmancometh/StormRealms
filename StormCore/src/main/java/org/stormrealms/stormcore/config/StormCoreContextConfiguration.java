package org.stormrealms.stormcore.config;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.bukkit.event.Listener;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormcore.StormPlugin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

@Configuration
@ComponentScan(basePackages = { "org.stormrealms.*" })
public class StormCoreContextConfiguration {

	Function<Path, URL> pathMapperFunc = (p) -> {
		try {
			return p.toUri().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	};

	@Bean
	public ScriptEngine basicScriptEngine() {
		return new ScriptEngineManager().getEngineByName("ecmascript");
	}

	@Bean("mod-paths")
	public List<URL> modulePaths(@Qualifier("modules-dir") File moduleDir) throws IOException {
		try (Stream<Path> pathStream = Files.walk(moduleDir.toPath().toAbsolutePath())) {
			return pathStream.filter(path1 -> path1.toString().endsWith(".jar")).map(pathMapperFunc)
					.collect(Collectors.toList());
		}
	}

	@Bean("classloader-map")
	public Map<String, URLClassLoader> loaderMap() {
		return new HashMap();
	}

	@Bean(name = "enabled-plugins")
	public Set<StormPlugin> enabledPlugins() {
		return new HashSet();
	}

	@Bean(name = "context-storage")
	public Map<Class<? extends StormPlugin>, AnnotationConfigApplicationContext> contexts() {
		return new ConcurrentHashMap();
	}

	@Bean(name = "listener-storage")
	public Multimap<Class<? extends StormPlugin>, Listener> listeners() {
		return Multimaps.synchronizedSetMultimap(HashMultimap.<Class<? extends StormPlugin>, Listener>create());
	}

	@Bean(name = "modules-dir")
	public File moduleDir() {
		return new File("plugins/");
	}

	@Bean
	@Scope("singleton")
	public ExecutorService pool() {
		return Executors.newFixedThreadPool(7);
	}

	@Bean
	public JSONParser parser() {
		JSONParser parser = new JSONParser();
		return parser;
	}

	@Bean
	public StormCore plugin() {
		return StormCore.getInstance();
	}

}
