package org.stormrealms.stormstats.configuration.pojo;

import org.stormrealms.stormmenus.Icon;
import org.stormrealms.stormmenus.MenuTemplate;

import lombok.Data;

@Data
public class GUIConfig {
	private Icon setName, setClass, setRace, charIcon, createChar;
	private MenuTemplate createCharTemplate;
	private int charsStartAt;
}
