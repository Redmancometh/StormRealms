package org.stormrealms.stormcore.command;

import java.util.Arrays;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.RedPlugin;
import org.stormrealms.stormcore.RedPlugins;
import org.stormrealms.stormcore.StormCore;

@Component
public class ModuleCommand {
    // TODO(Yevano)
    // @Autowired private StormCommandHandler handler;

    private void reload(String[] args, Player sender) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "/sc reload <module>");
        } else {
            String moduleName = args[0];

            var plugins = StormCore.getInstance().getPluginManager();

            Stream.<RedPlugin>generate(plugins.iterator()::next)
                .filter(elem -> moduleName.equalsIgnoreCase(elem.getClass().getName()))
                .findFirst()

                .ifPresentOrElse(module -> {
                    Class<? extends RedPlugin> moduleClass = module.getClass();

                    sender.sendMessage(String.format(
                        "%sAttempting to reload: %s...",
                        ChatColor.GREEN, moduleClass.getName()));
                    
                    if(RedPlugins.getInstance(moduleClass) != null) {
                        plugins.unloadPlugin(module.getClass());
                    }

                    module.disable();
                    module.enable();
                    
                    sender.sendMessage(String.format(
                        "%sReloaded module: %s",
                        ChatColor.GREEN, moduleClass.getName()));
                }, () -> {
                    sender.sendMessage(String.format(
                        "%sCannot find module with name: %s",
                        ChatColor.RED, moduleName));
                });
        }
    }

    // TODO(Yevano): More String.format, less debug prints.
    public void moduleCommand(String[] args, Player sender) {
        System.out.println("OP: " + sender.isOp());

		if (!sender.isOp()) {
			System.out.println("NOT OP");
            sender.sendMessage(ChatColor.RED + "You do not have permission to do this!");
            return;
        }
        
        if(args.length < 1) {
            sender.sendMessage(String.format("%sUsage: /sc <subcommand>", ChatColor.RED));
            return;
        }

        switch(args[0]) {
        case "reload":
            reload(Arrays.copyOfRange(args, 1, args.length), sender);
            return;
        }
    }

    // TODO(Yevano)
    @PostConstruct
    public void register() {
        //handler.registerCommand("sc", this::moduleCommand);
    }
}
