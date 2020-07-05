package org.stormrealms.stormstats.configuration.pojo;

import org.stormrealms.stormmenus.Icon;

import lombok.Data;

@Data
public class GUIConfig {
	private Icon setName, setClass, setRace, charIcon;
	private int charsStartAt;
}
