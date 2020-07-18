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
@SuppressWarnings("removal")
public class StormCommandHandler implements Listener {
	private HashMap<String, ScriptObjectMirror> commandMap = new HashMap<>();

	@EventHandler
	public void onCommandProcess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String fullCommand = event.getMessage();
		String[] allArgs = fullCommand.split(" ");
		String command = allArgs[0].replace("/", "");
<<<<<<< HEAD
        String[] args = Arrays.copyOfRange(allArgs, 1, allArgs.length);
        
        if(!commandMap.containsKey(command)) return;
		commandMap.get(command).accept(args, player);
=======
		String[] args = Arrays.copyOfRange(allArgs, 1, allArgs.length);
		if (commandMap.containsKey(command)) {
			commandMap.get(command).call(this, args, player);
			event.setCancelled(true);
		}
>>>>>>> 63dcc2ba2f6d45ebc6845551495bffe71bf8a049
	}

	public void registerCommand(String cmd, ScriptObjectMirror consumer) {
		this.commandMap.put(cmd, consumer);
	}

	public void unregisterCommand(String name) {
		commandMap.remove(name);
	}
}
