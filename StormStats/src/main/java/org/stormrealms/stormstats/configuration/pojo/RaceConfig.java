package org.stormrealms.stormstats.configuration.pojo;

import java.util.Map;

import lombok.Data;

@Data
public class RaceConfig {
	private Map<String, Race> races;

	public Race getRace(String race) {
		return races.get(race);
	}
}
