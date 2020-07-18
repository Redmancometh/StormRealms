package org.stormrealms.stormresources.configuration.pojo;

import java.util.Map;

import lombok.Data;

@Data
public class ResourceConfiguration {
	private Map<String, ResourceNode> resources;
}
