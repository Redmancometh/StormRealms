package org.stormrealms.stormloot.configuration.pojo;

import java.util.HashMap;
import java.util.Map;

import org.stormrealms.stormcore.outfacing.RPGStat;

import lombok.Getter;
import lombok.Setter;

public class LootRoll extends HashMap<RPGStat, Integer> {
	private static final long serialVersionUID = 4338734711786272126L;
	private Map<RPGStat, Integer> rolls = new HashMap();
	@Getter
	@Setter
	private String text;

	public void addRoll(RPGStat stat, int roll, String text) {
		rolls.put(stat, roll);
	}

}
