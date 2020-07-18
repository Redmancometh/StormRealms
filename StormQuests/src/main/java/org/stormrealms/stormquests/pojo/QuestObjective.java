package org.stormrealms.stormquests.pojo;

import lombok.Data;

@Data
public abstract class QuestObjective {
	private String completeDialogue;

	public boolean isComplete() {
		return false;
	}
}
