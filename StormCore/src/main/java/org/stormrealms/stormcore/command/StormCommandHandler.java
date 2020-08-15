package org.stormrealms.stormcore.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.BiConsumer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.springframework.stereotype.Component;

@Component
public class StormCommandHandler implements Listener {
	private HashMap<String, BiConsumer<Player, String[]>> commandMap = new HashMap<>();

	@EventHandler
	public void onCommandProcess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String fullCommand = event.getMessage();
		String[] allArgs = fullCommand.split(" ");
		String command = allArgs[0].replace("/", "");
		String[] args = Arrays.copyOfRange(allArgs, 1, allArgs.length);

		if (commandMap.containsKey(command)) {
			commandMap.get(command).accept(player, args);
			event.setCancelled(true);
		}
	}

	public void registerCommand(String cmd, BiConsumer<Player, String[]> consumer) {
		this.commandMap.put(cmd, consumer);
	}

	public void unregisterCommand(String name) {
		commandMap.remove(name);
	}
}
