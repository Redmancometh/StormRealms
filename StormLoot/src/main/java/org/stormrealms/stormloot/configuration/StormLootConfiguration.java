package org.stormrealms.stormloot.configuration;

import java.util.List;

import org.stormrealms.stormloot.configuration.pojo.LootPrefix;
import org.stormrealms.stormloot.configuration.pojo.LootSuffix;

import lombok.Data;

@Data
public class StormLootConfiguration {
	private List<LootPrefix> prefixes;
	private List<LootSuffix> suffixes;
}
