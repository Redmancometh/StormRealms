package org.stormrealms.stormcore.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ModuleCommand implements CommandExecutor {

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

		return false;
	}
}
