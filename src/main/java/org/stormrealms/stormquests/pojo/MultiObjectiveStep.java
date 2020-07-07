package org.stormrealms.stormquests.pojo;

import java.util.List;

import org.bukkit.scoreboard.Objective;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MultiObjectiveStep extends QuestStep {
	private List<Objective> objectives;

	@Override
	public boolean CheckComplete() {
		return false;
	}
}
