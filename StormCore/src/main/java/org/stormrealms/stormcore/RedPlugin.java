package org.stormrealms.stormcore;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;

public interface RedPlugin {
	JSONParser parser = new JSONParser();

	default JSONObject buildConfigFromPlugin() {
		File hibernateConfig = new File(
				new File("." + File.separator + "plugins" + File.separator + "StormCore" + File.separator),
				"config.json");
		// if (!hibernateConfig.exists())
		// plugin.saveResource("config.json", true);
		try (FileReader scanner = new FileReader(hibernateConfig)) {
			return (JSONObject) parser.parse(scanner);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException(
					"Configuration not initialized properly. Either config.json is missing, corrupted, or ill-formatted");
		}
	}

	default void disable() {
		StormCore.getInstance().getPluginManager().unloadPlugin(this.getClass());
	}

	default void enable() {
		StormCore.getInstance().getPluginManager().loadPlugin(this);
	}

	public default File getStormDirectory() {
		return new File("." + File.separator + "plugins" + File.separator + "StormCore" + File.separator);
	}

	public default File getDirectory() {
		return new File(getStormDirectory() + File.separator + getName());
	}

	/**
	 * This is only called once to build the Session Factory no need to cache it.
	 * 
	 * @return
	 */
	default JSONObject getConfiguration() {
		return buildConfigFromPlugin();
	}

	String getName();

	default void initialize() {

	}

}
