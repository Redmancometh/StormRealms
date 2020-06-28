package org.stormrealms.stormstats.configuration.pojo;

import java.util.Map;

import org.stormrealms.stormstats.model.PlayerStat;
import org.stormrealms.stormstats.model.RPGClass;

import lombok.Data;

@Data
public class DefaultsConfig {
	private Map<RPGClass, Map<PlayerStat, Integer>> stats;
	private double health;
}
