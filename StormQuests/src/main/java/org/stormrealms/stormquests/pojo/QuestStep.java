package org.stormrealms.stormquests.pojo;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public abstract class QuestStep {
	private String startDialogue;
	private String completeDialogue;
	List<QuestObjective> objectives;

	public abstract boolean CheckComplete();
}
