package org.stormrealms.stormquests.pojo;

import lombok.Data;

@Data
public abstract class QuestStep {
	private String startDialogue;
	private String completeDialogue;
	public abstract boolean CheckComplete();
}
