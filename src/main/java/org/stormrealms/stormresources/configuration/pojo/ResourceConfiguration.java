package org.stormrealms.stormresources.configuration.pojo;

import java.util.List;

import org.stormrealms.stormresources.configuration.ResourceNode;

import lombok.Data;

@Data
public class ResourceConfiguration {
	private List<ResourceNode> resources;
}
