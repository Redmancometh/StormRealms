package org.stormrealms.stormcombat.configuration.pojo;

import org.stormrealms.stormmenus.Icon;
import org.stormrealms.stormmenus.MenuTemplate;

import lombok.Data;

@Data
public class CombatGUIConfig {
	private Icon statIcon;
	private int startingIndex;
	private MenuTemplate statTemplates;
}
