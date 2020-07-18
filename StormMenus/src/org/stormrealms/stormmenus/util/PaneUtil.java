package org.stormrealms.stormmenus.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;

public class PaneUtil {
	private static Map<Integer, Material> colorMap = new ConcurrentHashMap();
	static {
		colorMap.put(0, Material.WHITE_STAINED_GLASS_PANE);
		colorMap.put(1, Material.ORANGE_STAINED_GLASS_PANE);
		colorMap.put(2, Material.MAGENTA_STAINED_GLASS_PANE);
		colorMap.put(3, Material.LIGHT_BLUE_STAINED_GLASS_PANE);
		colorMap.put(4, Material.YELLOW_STAINED_GLASS_PANE);
		colorMap.put(5, Material.LIME_STAINED_GLASS_PANE);
		colorMap.put(6, Material.PINK_STAINED_GLASS_PANE);
		colorMap.put(7, Material.GRAY_STAINED_GLASS_PANE);
		colorMap.put(8, Material.LIGHT_GRAY_STAINED_GLASS_PANE);
		colorMap.put(9, Material.CYAN_STAINED_GLASS_PANE);
		colorMap.put(10, Material.PURPLE_STAINED_GLASS_PANE);
		colorMap.put(11, Material.BLUE_STAINED_GLASS_PANE);
		colorMap.put(12, Material.BROWN_STAINED_GLASS_PANE);
		colorMap.put(13, Material.GREEN_STAINED_GLASS_PANE);
		colorMap.put(14, Material.RED_STAINED_GLASS_PANE);
		colorMap.put(15, Material.BLACK_STAINED_GLASS_PANE);
	}

	public static Material getPaneFor(int dv) {
		return colorMap.get(dv);
	}
}
