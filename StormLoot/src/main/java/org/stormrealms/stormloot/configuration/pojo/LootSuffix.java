package org.stormrealms.stormloot.configuration.pojo;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.springframework.beans.factory.annotation.Autowired;
import org.stormrealms.stormcore.outfacing.RPGStat;

import lombok.Data;

@Data
public class LootSuffix {
	private String suffix;
	private Map<RPGStat, String> mappingFunctionMap = new HashMap();
	@Autowired
	private ScriptEngine scriptEngine;

	public LootRoll rollStats(int level) {
		LootRoll roll = new LootRoll();
		mappingFunctionMap.forEach((stat, evalString) -> {
			try {
				roll.addRoll(stat, (Integer) scriptEngine.eval(evalString.replace("x", level + "")), this.getSuffix());
			} catch (ScriptException e) {
				e.printStackTrace();
			}
		});
		return roll;
	}
}
