package org.stormrealms.stormstats.configuration.pojo;

import org.stormrealms.stormmenus.Icon;

import lombok.Data;

@Data
public class Race {
	private String name;
	private Icon raceIcon;
	private Integer bonusAgi, bonusInt, bonusStr, bonusSpi;
}
