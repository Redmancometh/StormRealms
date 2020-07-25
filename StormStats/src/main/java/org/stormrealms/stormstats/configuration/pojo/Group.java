package org.stormrealms.stormstats.configuration.pojo;

import java.util.List;

import lombok.Data;

@Data
public class Group {
	private String name, key;
	private List<String> permissions;
}
