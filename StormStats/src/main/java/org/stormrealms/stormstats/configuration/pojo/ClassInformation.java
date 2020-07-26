package org.stormrealms.stormstats.configuration.pojo;

import java.util.Map;

import org.stormrealms.stormcore.outfacing.RPGStat;
import org.stormrealms.stormmenus.Icon;

import lombok.Data;

@Data
public class ClassInformation {
	private String className, key;
	private Icon classItem;
	private Map<RPGStat, Integer> startingStats;
}
