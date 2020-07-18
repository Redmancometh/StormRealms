package org.stormrealms.stormquests.controller;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.stormrealms.stormquests.pojo.Quest;

public class KillListeners implements Listener {
	@Autowired
	@Qualifier("quests")
	private Map<Integer, Quest> quests;

	@EventHandler
	public void onDeath(EntityDeathEvent e) {
		Player p = e.getEntity().getKiller();
		if (p != null) {
			/*
			 * stats.getRecord(p.getUniqueId()).thenAccept((rpgPlayer) -> { RPGCharacter
			 * character = rpgPlayer.getChosenCharacter();
			 * character.getQuestMap().forEach((id, stepNo) -> { Quest quest =
			 * quests.get(id); QuestStep step = quest.getSteps().get(stepNo);
			 * step.getObjectives().forEach((objective) -> {
			 * 
			 * }); }); });
			 */
		}
	}
}
