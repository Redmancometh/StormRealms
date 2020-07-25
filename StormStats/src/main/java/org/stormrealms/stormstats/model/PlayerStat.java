package org.stormrealms.stormstats.model;

public enum PlayerStat {
	STR("Strength"), AGI("Agility"), SPI("Spirit"), STA("Stamina");
	private String name;

	PlayerStat(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
