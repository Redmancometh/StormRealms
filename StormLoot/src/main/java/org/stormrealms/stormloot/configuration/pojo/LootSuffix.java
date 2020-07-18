package org.stormrealms.stormloot.configuration.pojo;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormcore.outfacing.RPGStat;

import lombok.Data;

@Data
public class LootSuffix {
	private String suffix;
	private Map<RPGStat, String> mappingFunctionMap = new HashMap();

	public LootRoll rollStats(int level) {
		LootRoll roll = new LootRoll();
		roll.setText(suffix);
		mappingFunctionMap.forEach((stat, evalString) -> {
			ScriptEngine scriptEngine = StormCore.getInstance().getContext().getAutowireCapableBeanFactory()
					.getBean(ScriptEngine.class);
			try {
				roll.addRoll(stat, (Integer) scriptEngine.eval(evalString.replace("x", level + "")), this.getSuffix());
			} catch (ScriptException e) {
				e.printStackTrace();
			}
		});
		return roll;
	}
}
