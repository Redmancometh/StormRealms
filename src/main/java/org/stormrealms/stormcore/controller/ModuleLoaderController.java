package org.stormrealms.stormcore.controller;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.stormrealms.stormcore.StormPlugin;
import org.stormrealms.stormcore.util.PluginConfig;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import javax.annotation.PostConstruct;

@Controller
public class ModuleLoaderController {

	private File moduleDir;
	private Set<StormPlugin> enabledPlugins;
	private Set<StormPlugin> plugins;
	@Autowired
	private ApplicationContext context;

	public ModuleLoaderController(@Autowired @Qualifier("modules-dir") File moduleDir) {
		this.moduleDir = moduleDir;
		this.enabledPlugins = new HashSet<>();
		this.plugins = new HashSet<>();
	}

	@PostConstruct
	public void loadModules() {

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
		if (!path.toFile().exists()) {
			throw new Exception(String.format("Could not find module at %s", path.toAbsolutePath()));
		}

		JarFile file = new JarFile(path.toFile());
		ZipEntry moduleJson = file.getEntry("module.json");

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
		Class mainClass = classLoader.loadClass(config.getMain());

		StormPlugin module = (StormPlugin) mainClass.newInstance();
		System.out.println(module.getConfigurationClass().getName());

		module.setName(config.getName());

		/**
		 * Field versionField =
		 * module.getClass().getSuperclass().getDeclaredField("version");
		 * versionField.setAccessible(true); versionField.set(module,
		 * config.getVersion()); versionField.setAccessible(false);
		 * 
		 * Field descField =
		 * module.getClass().getSuperclass().getDeclaredField("description");
		 * descField.setAccessible(true); descField.set(module,
		 * config.getDescription()); versionField.setAccessible(false);
		 **/

		/*
		 * Field commandField =
		 * module.getClass().getSuperclass().getDeclaredField("commands");
		 * commandField.setAccessible(true); commandField.set(module,
		 * config.getCommands()); versionField.setAccessible(false);
		 */

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

	public StormPlugin byName(String name) {
		return enabledPlugins.stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}
}
