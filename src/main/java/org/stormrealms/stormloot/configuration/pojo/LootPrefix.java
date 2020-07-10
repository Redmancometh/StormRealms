package org.stormrealms.stormloot.configuration.pojo;

import java.util.HashMap;
import java.util.Map;

import org.stormrealms.stormcore.outfacing.RPGStat;

import lombok.Data;

@Data
public class LootPrefix {
	private String prefix;
	private Map<RPGStat, String> mappingFunctionMap = new HashMap();
	
}
