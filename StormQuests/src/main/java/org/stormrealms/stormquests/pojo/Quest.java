package org.stormrealms.stormquests.pojo;

import java.util.List;

import lombok.Data;

@Data
public class Quest {
	private int id;
	private List<QuestStep> steps;
	private QuestStart start;
}
