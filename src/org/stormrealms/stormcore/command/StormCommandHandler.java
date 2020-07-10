package org.stormrealms.stormcore.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.controller.ModuleLoaderController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class StormCommandHandler implements Listener {
	private HashMap<String, SCommandExecutor> commandMap = new HashMap<>();
	@Autowired
	private ModuleLoaderController moduleLoaderController;

	@EventHandler
	public void onCommandProcess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String fullCommand = event.getMessage();

		String[] allArgs = fullCommand.split(" ");
		String command = allArgs[0].replace("/", "");

		String[] args = Arrays.copyOfRange(allArgs, 1, allArgs.length);
		if (args.length == 2 && args[0].equalsIgnoreCase("reload")) {
			String moduleName = args[1];
			player.sendMessage(ChatColor.GREEN + "Attempting to reload: " + moduleName + "...");
			this.moduleLoaderController.reloadModule(moduleLoaderController.byName(moduleName));
			player.sendMessage(ChatColor.GREEN + "Reloaded module: " + moduleName);
		}
		SCommandExecutor exec = byCommand(command);
		if (exec == null) {
			player.sendMessage(ChatColor.RED + "You do not have permission for this command!");
			return;
		}
		// TODO priority
		exec.execute(player, args);

	}

	public SCommandExecutor byCommand(String command) {
		for (Map.Entry<String, SCommandExecutor> entry : commandMap.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(command)) {
				return entry.getValue();
			}
		}
		return null;
	}

	public void registerCommand(String cmd, SCommandExecutor executor) {
		executor.setName(cmd);
		this.commandMap.put(cmd, executor);
	}

	public void unregisterCommand(SCommandExecutor executor) {
		String remove = "";
		for (Map.Entry<String, SCommandExecutor> entry : commandMap.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(executor.getName())) {
				remove = entry.getKey();
			}
		}

		if (remove.equals("")) {
			return;
		}
		this.commandMap.remove(remove);
	}
}
