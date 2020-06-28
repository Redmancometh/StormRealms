package org.stormrealms.stormstats.configuration.pojo;

import java.util.List;

import org.bukkit.Material;

import lombok.Data;

@Data
public class ClassInformation {
	private String className;
	private List<String> classLore;
	private Material classItem;
	private int startingAgi, startingStr, startingSpi, startingIntel;
}
