package org.stormrealms.stormloot.configuration.pojo;

import java.util.Map;
import lombok.Data;

@Data
public class ItemEffect {
	private Map<Class, String> events;
}
