package org.stormrealms.stormstats.configuration.pojo;

import org.stormrealms.stormmenus.Icon;

import lombok.Data;

@Data
public class ClassInformation {
	private String className;
	private Icon classItem;
	private int startingAgi, startingStr, startingSpi, startingIntel;
}
