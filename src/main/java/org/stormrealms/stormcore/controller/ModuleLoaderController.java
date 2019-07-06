package org.stormrealms.stormcore.controller;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.stormrealms.stormcore.StormPlugin;
import org.stormrealms.stormcore.util.PluginConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import javax.annotation.PostConstruct;

@Controller
public class ModuleLoaderController {

	private File moduleDir;
	private Set<StormPlugin> enabledPlugins;
	private Set<StormPlugin> plugins;

	public ModuleLoaderController(@Autowired @Qualifier("modules-dir") File moduleDir) {
		this.moduleDir = moduleDir;
		this.enabledPlugins = new HashSet<>();
		this.plugins = new HashSet<>();
	}

	@PostConstruct
	public void loadModules() {
		System.out.println("LOAD MODULES!");
		if (!moduleDir.exists()) {
			moduleDir.mkdir();
		}
		try (Stream<Path> pathStream = Files.walk(moduleDir.toPath().toAbsolutePath())) {
			pathStream.filter(path1 -> path1.toString().endsWith(".jar")).forEach(p -> {
				try {
					StormPlugin pluginModule = loadModule(p);
					this.plugins.add(pluginModule);
					enableModule(pluginModule);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public StormPlugin loadModule(Path path) throws Exception {
		AnnotationConfigApplicationContext moduleContext = new AnnotationConfigApplicationContext();
		if (!path.toFile().exists()) {
			throw new Exception(String.format("Could not find module at %s", path.toAbsolutePath()));
		}

		JarFile file = new JarFile(path.toFile());
		ZipEntry moduleJson = file.getEntry("module.json");

		JarInputStream jarInputStream = new JarInputStream(new FileInputStream(path.toFile()));

		System.out.println(path.toUri().toURL());

		if (moduleJson == null) {
			throw new Exception(String.format("Could not find module json at %s", path.toAbsolutePath()));
		}

		String moduleJsonSTR = CharStreams
				.toString(new InputStreamReader(file.getInputStream(moduleJson), Charsets.UTF_8));
		Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
				.setPrettyPrinting().create();

		PluginConfig config = gson.fromJson(moduleJsonSTR, PluginConfig.class);

//        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{path.toUri().toURL()});
		URLClassLoader classLoader = new URLClassLoader(new URL[] { path.toUri().toURL() },
				getClass().getClassLoader());
		moduleContext.setClassLoader(classLoader);
		Class mainClass = classLoader.loadClass(config.getMain());

		Enumeration<JarEntry> entries = file.entries();

		while (entries.hasMoreElements()) {
			JarEntry clazz = entries.nextElement();
			if (clazz.getName().equals(mainClass.getName())) {
				System.out.println("FOUND MAIN AND SKIPPING");
				continue;
			}
			if (clazz.isDirectory() || !clazz.getName().endsWith(".class")) {
				continue;
			}
			String className = clazz.getName().substring(0, clazz.getName().length() - 6);
			className = className.replace('/', '.');
			Class<?> clazzLoaded = classLoader.loadClass(className);
			for (Annotation annotation : clazzLoaded.getAnnotations()) {
				if (annotation.annotationType().isAssignableFrom(Component.class)) {
					moduleContext.register(clazzLoaded);
				}
			}

		}
		StormPlugin module = (StormPlugin) mainClass.newInstance();
		module.setContext(moduleContext);
		System.out.println(module.getConfigurationClass().getName());
		module.setName(config.getName());
		file.close();
		classLoader.close();
		return module;
	}

	public void enableModule(StormPlugin plugin) {
		if (enabledPlugins.contains(plugin)) {
			return;
		}

		plugin.enable();
		this.enabledPlugins.add(plugin);
	}

	public void disableModule(String name) {
		StormPlugin p = byName(name);
		disableModule(p);
	}

	public void disableModule(StormPlugin plugin) {
		if (plugin == null) {
			return;
		}
		plugin.disable();
		this.enabledPlugins.remove(plugin);
	}

	public File findModule(String name) {
		return Arrays.asList(moduleDir.listFiles()).stream().filter(file -> file.getName().contains(name)).findFirst()
				.orElse(null);
	}

	public Set<StormPlugin> getEnabledPlugins() {
		return enabledPlugins;
	}

	public StormPlugin byName(String name) {
		return enabledPlugins.stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}
}
