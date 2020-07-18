package org.stormrealms.stormloot.configuration.pojo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormcore.outfacing.RPGStat;

import lombok.Data;

@Data
public class LootPrefix {
	private String prefix;
	private Map<RPGStat, String> mappingFunctionMap = new HashMap();
	private List<Integer> itemEffects;

	public LootRoll rollStats(int level) {
		LootRoll roll = new LootRoll();
		roll.setText(prefix);
		mappingFunctionMap.forEach((stat, evalString) -> {
			ScriptEngine scriptEngine = StormCore.getInstance().getContext().getAutowireCapableBeanFactory()
					.getBean(ScriptEngine.class);
			try {
				roll.addRoll(stat, (Integer) scriptEngine.eval(evalString.replace("x", level + "")), this.getPrefix());
			} catch (ScriptException e) {
				e.printStackTrace();
			}
		});
		return roll;
	}
}
