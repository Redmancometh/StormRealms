package org.stormrealms.stormcore.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormcore.outfacing.RPGStat;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author Redmancometh
 *
 * @param <T>
 */
@Data
public class ConfigManager<T> {
	@Getter
	protected Gson gson;
	protected String fileName;
	protected Class<T> clazz;
	protected T config;
	private FileWatcher watcher;
	@Getter
	@Setter
	private Runnable onReload;

	public ConfigManager(String fileName, Class<T> clazz) {
		this(fileName, clazz, null);
	}

	public ConfigManager(String fileName, Class<T> clazz, Runnable onReload) {
		this(fileName, clazz, onReload, null);
	}

	public ConfigManager(String fileName, Class<T> clazz, Runnable onReload, GsonBuilder gsonBuilder) {
		super();
		this.fileName = fileName;
		this.clazz = clazz;
		this.onReload = onReload;
		if(gsonBuilder == null) gsonBuilder = new GsonBuilder();
		gson = gsonBuilder.excludeFieldsWithModifiers(Modifier.PROTECTED)
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
			.registerTypeHierarchyAdapter(String.class, new PathAdapter())
			.registerTypeHierarchyAdapter(Material.class, new MaterialAdapter())
			.registerTypeAdapter(Location.class, new LocationAdapter())
			.registerTypeAdapter(RPGStat.class, new RPGStatAdapter())
			.registerTypeHierarchyAdapter(Class.class, new ClassAdapter()).setLenient().setPrettyPrinting().create();
	}

	public void init() {

		initConfig();
		registerMonitor();
	}

	/**
	 * Register the file monitor
	 * 
	 * TODO: This will reload every config any time ANYTHING in the config dir is
	 * changed. So compartmentalize this later.
	 * 
	 */
	public void registerMonitor() {
		watcher = new FileWatcher((file) -> {
			System.out.println("Reloaded: " + file);
			this.initConfig();
			if (this.onReload != null)
				this.onReload.run();
		}, new File("config" + File.separator + this.fileName));
		watcher.start();
	}

	public void writeConfig() {
		try (FileWriter w = new FileWriter("config" + File.separator + this.fileName)) {
			getGson().toJson(config, w);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveConfig() {
		try (FileWriter writer = new FileWriter(new File("config" + File.separator + this.fileName))) {
			gson.toJson(getConfig(), writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void initConfig() {
		try (FileReader reader = new FileReader("config" + File.separator + fileName)) {
			T conf = getGson().fromJson(reader, clazz);
			this.config = conf;
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	public T targetUnit() {
		return config;
	}

	public void setConfig(T config) {
		this.config = config;
	}

	public static class RPGStatAdapter extends TypeAdapter<RPGStat> {

		@Override
		public RPGStat read(JsonReader arg0) throws IOException {
			String materialValue = arg0.nextString();
			RPGStat stat = RPGStat.valueOf(materialValue.replace(" ", "_").toUpperCase());
			return stat;
		}

		@Override
		public void write(JsonWriter arg0, RPGStat arg1) throws IOException {
			arg0.value(arg1.toString());
		}
	}

	public static class MaterialAdapter extends TypeAdapter<Material> {

		@Override
		public Material read(JsonReader arg0) throws IOException {
			String materialValue = arg0.nextString();
			return Material.valueOf(materialValue.replace(" ", "_").toUpperCase());
		}

		@Override
		public void write(JsonWriter arg0, Material arg1) throws IOException {
			arg0.value(arg1.toString());
		}
	}

	public static class ClassAdapter extends TypeAdapter<Class<?>> {
		@Override
		public void write(JsonWriter jsonWriter, Class<?> material) throws IOException {

		}

		@Override
		public Class<?> read(JsonReader jsonReader) throws IOException {
			var classLoader = StormCore.getInstance().getContext().getClassLoader();
			String className = jsonReader.nextString();
			try {
				return classLoader.loadClass(className);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public static class LocationAdapter extends TypeAdapter<Location> {
		@Override
		public Location read(JsonReader reader) throws IOException {
			reader.beginObject();
			JsonToken token = reader.peek();
			Double x = null;
			Double y = null;
			Double z = null;
			String worldName = null;
			while (reader.hasNext()) {
				if (token.equals(JsonToken.NAME)) {
					String fieldName = reader.nextName();
					if (fieldName.equalsIgnoreCase("x")) {
						System.out.println("X");
						x = reader.nextDouble();
					} else if (fieldName.equalsIgnoreCase("y")) {
						System.out.println("Y");
						y = reader.nextDouble();
					} else if (fieldName.equalsIgnoreCase("z")) {
						System.out.println("Z");
						z = reader.nextDouble();
					} else if (fieldName.equalsIgnoreCase("world")) {
						System.out.println("WORLD");
						worldName = reader.nextString();
					}
				}
			}
			if (x == null)
				throw new IllegalStateException("Invalid config on location (x is null) " + reader.getPath());
			else if (y == null)
				throw new IllegalStateException("Invalid config on location (y is null) " + reader.getPath());
			else if (z == null)
				throw new IllegalStateException("Invalid config on location (z is null) " + reader.getPath());
			else if (worldName == null)
				throw new IllegalStateException("Invalid world on location " + reader.getPath());
			if (Bukkit.getWorld(worldName) == null)
				// might not want to use this.
				throw new IllegalStateException(
						"Location at " + reader.getPath() + " contains areference to a world that does not exist!");
			reader.endObject();
			return new Location(Bukkit.getWorld(worldName), x.doubleValue(), y.doubleValue(), z.doubleValue());
		}

		@Override
		public void write(JsonWriter arg0, Location arg1) throws IOException {
			arg0.value(arg1.toString());
		}

	}
	
	public static class ClassAdapterTwo extends TypeAdapter<Class<?>> {
		@Override
		public Class<?> read(JsonReader arg0) throws IOException {
			String string = arg0.nextString();
			try {
				return Class.forName(string);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			return null;
			
		}

		@Override
		public void write(JsonWriter arg0, Class<?> arg1) throws IOException {
			arg0.value(arg1.getName());
		}

	
	}

	public static class PathAdapter extends TypeAdapter<String> {
		@Override
		public String read(JsonReader arg0) throws IOException {
			String string = arg0.nextString();
			if (string.contains("http"))
				return string;
			return ChatColor.translateAlternateColorCodes('&',
					string.replace("//", File.separator).replace("\\", File.separator));
		}

		@Override
		public void write(JsonWriter arg0, String arg1) throws IOException {
			arg0.value(arg1);
		}
	}

}
