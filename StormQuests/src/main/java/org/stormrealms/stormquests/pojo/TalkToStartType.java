package org.stormrealms.stormquests.pojo;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TalkToStartType extends QuestStart {
	private int id;
	private List<String> startDialogue;
}
