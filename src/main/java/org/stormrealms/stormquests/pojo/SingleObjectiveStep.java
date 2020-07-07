package org.stormrealms.stormquests.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SingleObjectiveStep extends QuestStep {
	private QuestObjective objective;

	@Override
	public boolean CheckComplete() {
		return false;
	}
}
