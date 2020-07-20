package org.stormrealms.stormstats.configuration.pojo;

import java.util.Map;

import org.stormrealms.stormcore.outfacing.RPGStat;
import org.stormrealms.stormmenus.Icon;

import lombok.Data;

@Data
public class Race {
	private String name;
	private Icon raceIcon;
	private Map<RPGStat, Integer> bonusStats;
}
