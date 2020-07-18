package org.stormrealms.stormloot.configuration.pojo;

import java.util.Map;

import lombok.Data;

@Data
public class ItemEffects {
	private Map<Integer, ItemEffect> effects;
}
