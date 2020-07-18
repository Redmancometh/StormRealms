package org.stormrealms.stormcore.command;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.BiConsumer;

@Component
public class StormCommandHandler implements Listener {
	private HashMap<String, BiConsumer<String[], Player>> commandMap = new HashMap<>();

	@EventHandler
	public void onCommandProcess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String fullCommand = event.getMessage();
		String[] allArgs = fullCommand.split(" ");
		String command = allArgs[0].replace("/", "");
		String[] args = Arrays.copyOfRange(allArgs, 1, allArgs.length);
		commandMap.get(command).accept(args, player);
	}

	public void registerCommand(String cmd, BiConsumer<String[], Player> executor) {
		this.commandMap.put(cmd, executor);
	}

	public void unregisterCommand(String name) {
		commandMap.remove(name);
	}
}
