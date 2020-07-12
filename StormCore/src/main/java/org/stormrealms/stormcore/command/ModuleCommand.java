package org.stormrealms.stormcore.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.stormrealms.stormcore.StormPlugin;
import org.stormrealms.stormcore.controller.ModuleLoaderController;

import java.io.File;
import java.io.IOException;

public class ModuleCommand implements CommandExecutor {

	@Autowired
	private ModuleLoaderController moduleLoaderController;

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
		if (!cmd.getName().equalsIgnoreCase("sc")) {
			return true;
		}
		System.out.println("OP: " + sender.isOp());
		if (!sender.isOp()) {
			System.out.println("NOT OP");
			sender.sendMessage(ChatColor.RED + "You do not have permission to do this!");
			return true;
		}

		if (args[0].equalsIgnoreCase("enable")) {

			if (args.length != 2) {
				sender.sendMessage(ChatColor.RED + "/sc enable [module]");
			} else {
				String moduleName = args[1];

				StormPlugin module = this.moduleLoaderController.byName(moduleName);
				if (module == null) {
					sender.sendMessage(ChatColor.RED + "Cannot find module with name: " + moduleName);
					return true;
				}

				sender.sendMessage(ChatColor.GREEN + "Attempting to enable: " + moduleName + "...");
				this.moduleLoaderController.enableModule(moduleLoaderController.byName(moduleName));
				sender.sendMessage(ChatColor.GREEN + "Enabled module: " + moduleName);
			}

		} else if (args[0].equalsIgnoreCase("reload")) {
			if (args.length != 2) {
				sender.sendMessage(ChatColor.RED + "/sc reload [module]");
			} else {
				String moduleName = args[1];

				StormPlugin module = this.moduleLoaderController.byName(moduleName);
				if (module == null) {
					sender.sendMessage(ChatColor.RED + "Cannot find module with name: " + moduleName);
					return true;
				}

				sender.sendMessage(ChatColor.GREEN + "Attempting to reload: " + moduleName + "...");
				// this.moduleLoaderController.reloadModule(moduleLoaderController.byName(moduleName));
				sender.sendMessage(ChatColor.GREEN + "Reloaded module: " + moduleName);
			}
		} else if (args[0].equalsIgnoreCase("disable")) {
			if (args.length != 2) {
				sender.sendMessage(ChatColor.RED + "/sc disable [module]");
			} else {
				String moduleName = args[1];

				StormPlugin module = this.moduleLoaderController.byName(moduleName);
				if (module == null) {
					sender.sendMessage(ChatColor.RED + "Cannot find module with name: " + moduleName);
					return true;
				}

				sender.sendMessage(ChatColor.GREEN + "Attempting to disable: " + moduleName + "...");
				try {
					this.moduleLoaderController.disableModule(moduleLoaderController.byName(moduleName));
					sender.sendMessage(ChatColor.GREEN + "Disabled module: " + moduleName);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (args[0].equalsIgnoreCase("load")) {
			// TODO make a loadModule by name and find jar with that name
			if (args.length != 2) {
				sender.sendMessage(ChatColor.RED + "/sc load [module]");
			} else {
				String moduleName = args[1];

				File module = this.moduleLoaderController.findModule(moduleName);
				if (module == null) {
					sender.sendMessage(ChatColor.RED + "Cannot find module file with name: " + moduleName);
					return true;
				}

				sender.sendMessage(ChatColor.GREEN + "Attempting to load: " + moduleName + "...");
				try {
					this.moduleLoaderController.queueModule(module.toPath());
				} catch (Exception e) {
					e.printStackTrace();
				}
				sender.sendMessage(ChatColor.GREEN + "Loaded module: " + moduleName);
			}
		} else if (args[0].equalsIgnoreCase("list")) {

			sender.sendMessage(ChatColor.GOLD + "Enabled modules:");
			for (StormPlugin plugin : moduleLoaderController.getEnabledPlugins()) {
				sender.sendMessage(ChatColor.GREEN + plugin.getName());
			}
		}

		return false;
	}
}
