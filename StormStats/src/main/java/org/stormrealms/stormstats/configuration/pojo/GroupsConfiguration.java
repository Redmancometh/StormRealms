package org.stormrealms.stormstats.configuration.pojo;

import java.util.Map;

import lombok.Data;

@Data
public class GroupsConfiguration {
	private Map<String, Group> groups;
}
