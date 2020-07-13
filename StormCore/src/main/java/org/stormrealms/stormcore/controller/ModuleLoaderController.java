package org.stormrealms.stormcore.controller;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.stormrealms.stormcore.DBRedPlugin;
import org.stormrealms.stormcore.SpringPlugin;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormcore.StormPlugin;
import org.stormrealms.stormcore.StormSpringPlugin;
import org.stormrealms.stormcore.config.pojo.PluginLoadTask;
import org.stormrealms.stormcore.config.pojo.PluginLoadTaskContainer;
import org.stormrealms.stormcore.util.PluginConfig;
import org.stormrealms.stormcore.util.SpringUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

@Controller
@Order(0)
/**
 * Defunct.
 * 
 * @author Redmancometh
 *
 */
public class ModuleLoaderController {
	private File moduleDir;
	@Autowired
	private PluginLoadTaskContainer container;
	@Autowired
	@Qualifier("enabled-plugins")
	private Set<StormPlugin> enabledPlugins;
	@Autowired
	@Qualifier("classloader-map")
	private Map<String, URLClassLoader> loaderMap;

	public ModuleLoaderController(@Autowired @Qualifier("modules-dir") File moduleDir) {
		this.moduleDir = moduleDir;
	}

	public void loadModules() {

		if (!moduleDir.exists())
			moduleDir.mkdir();
		try (Stream<Path> pathStream = Files.walk(moduleDir.toPath().toAbsolutePath())) {
			pathStream.filter(path1 -> path1.toString().endsWith(".jar")).forEach(p -> {
				try {
					System.out.println("Queueing " + p.toFile());
					queueModule(p);
				} catch (IllegalStateException e) {
					System.out.println("No module.json found in " + p.toFile() + " skipping.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int x = 0; x < 50; x++) {
			if (!container.hasPass(x))
				continue;
			System.out.println("Loading plugin layer #" + x);
			for (PluginLoadTask task : container.getPass(x)) {
				loadPlugin(task, x);
			}
		}

	}

	public void loadPlugin(PluginLoadTask task, int pluginIndex) {

		try {
			PluginConfig config = task.getConfig();
			StormSpringPlugin module = null;
			Path path = Paths.get("plugins/StormCore/modules/" + config.getName() + ".jar");
			System.out.println(path.toUri().toURL());
			URLClassLoader moduleLoader = new URLClassLoader(config.getName(), new URL[] { path.toUri().toURL() },
					StormCore.getInstance().getPLClassLoader());
			loaderMap.put(config.getName(), moduleLoader);
			Class mainClass = StormCore.getInstance().getPLClassLoader().loadClass(config.getMain());
			SpringUtil.addSpringBean(mainClass, config.getName(), BeanDefinition.SCOPE_SINGLETON);
			module = (StormSpringPlugin) StormCore.getInstance().getContext().getAutowireCapableBeanFactory()
					.getBean(mainClass);
			module.setModuleLoader(moduleLoader);
			module.setName(config.getName());
			System.out.println("Loading spring plugin with config " + config + " during pass #" + pluginIndex);
			loadSpringPlugin((StormSpringPlugin) module, config);
			if (module instanceof DBRedPlugin)
				((DBRedPlugin) module).initialize();
			enabledPlugins.add(module);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private StormSpringPlugin loadSpringPlugin(StormSpringPlugin module, PluginConfig config) throws IOException {
		AnnotationConfigApplicationContext moduleContext = new AnnotationConfigApplicationContext();
		moduleContext.setParent(StormCore.getInstance().getContext());
		moduleContext.setClassLoader(StormCore.getInstance().getPLClassLoader());
		SpringPlugin spModule = (SpringPlugin) module;
		moduleContext.setParent(StormCore.getInstance().getContext());
		spModule.setContext(moduleContext);
		module.enable();
		module.setName(config.getName());
		moduleContext.scan(spModule.getPackages());
		moduleContext.register(spModule.getConfigurationClass());
		moduleContext.refresh();
		return module;
	}

	public StormPlugin loadStormPlugin(StormPlugin module, PluginConfig config) throws IOException {
		Path path = Paths.get("plugins/StormCore/modules/" + module.getName() + ".jar");
		URLClassLoader moduleLoader = new URLClassLoader(module.getName(), new URL[] { path.toUri().toURL() },
				StormCore.getInstance().getPLClassLoader());
		loaderMap.put(module.getName(), StormCore.getInstance().getPLClassLoader());
		module.setModuleLoader(moduleLoader);
		module.enable();
		module.setName(config.getName());
		return module;
	}

	public PluginConfig getPluginConfigFromPath(Path path) throws Exception {
		if (!path.toFile().exists())
			throw new Exception(String.format("Could not find module at %s", path.toAbsolutePath()));
		JarFile file = new JarFile(path.toFile());
		ZipEntry moduleJson = file.getEntry("module.json");
		if (moduleJson == null)
			throw new IllegalStateException(String.format("Could not find module json at %s", path.toAbsolutePath()));
		String moduleJsonSTR = CharStreams
				.toString(new InputStreamReader(file.getInputStream(moduleJson), Charsets.UTF_8));
		Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES).setPrettyPrinting()
				.create();
		PluginConfig config = gson.fromJson(moduleJsonSTR, PluginConfig.class);
		return config;
	}

	public void queueModule(Path path) throws Exception {
		container.addTask(new PluginLoadTask(getPluginConfigFromPath(path)));
	}

	public void enableModule(StormPlugin plugin) {
		if (enabledPlugins.contains(plugin)) {
			return;
		}
		this.enabledPlugins.add(plugin);
	}

	public void disableModule(String name) {
		StormPlugin p = byName(name);
		try {
			disableModule(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void disableModule(StormPlugin plugin) throws IOException {
		if (plugin == null) {
			return;
		}
		plugin.disable();
		if (plugin instanceof SpringPlugin)
			((SpringPlugin) plugin).getContext().close();
		this.enabledPlugins.remove(plugin);
		this.loaderMap.get(plugin.getName()).close();
		this.loaderMap.remove(plugin.getName());
		plugin.getModuleLoader().close();
	}

	public File findModule(String name) {
		return Arrays.asList(moduleDir.listFiles()).stream().filter(file -> file.getName().contains(name)).findFirst()
				.orElse(null);
	}

	public Set<StormPlugin> getEnabledPlugins() {
		return enabledPlugins;
	}

	public StormPlugin byName(String name) {
		System.out.println("PLUGIN SIZE " + enabledPlugins.size());
		enabledPlugins.forEach((plugin) -> plugin.getName());
		return enabledPlugins.stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	public void reloadModule(StormPlugin pl) {
		Path path = Paths.get("plugins/StormCore/modules/" + pl.getName() + ".jar");
		try {
			PluginLoadTask task = new PluginLoadTask(getPluginConfigFromPath(path));
			disableModule(pl);
			loadPlugin(task, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
