package org.stormrealms.stormcore.command;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.springframework.stereotype.Component;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.util.Arrays;
import java.util.HashMap;

@Component
public class StormCommandHandler implements Listener {
	private HashMap<String, ScriptObjectMirror> commandMap = new HashMap<>();

	@EventHandler
	public void onCommandProcess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String fullCommand = event.getMessage();
		String[] allArgs = fullCommand.split(" ");
		String command = allArgs[0].replace("/", "");
		String[] args = Arrays.copyOfRange(allArgs, 1, allArgs.length);
		if (commandMap.containsKey(command)) {
			commandMap.get(command).call(this, args, player);
			event.setCancelled(true);
		}
	}

	public void registerCommand(String cmd, ScriptObjectMirror consumer) {
		this.commandMap.put(cmd, consumer);
	}

	public void unregisterCommand(String name) {
		commandMap.remove(name);
	}
}
