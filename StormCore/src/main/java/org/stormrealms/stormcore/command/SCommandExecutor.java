package org.stormrealms.stormcore.command;

import org.bukkit.command.CommandSender;

public abstract class SCommandExecutor {
    private String name;

    public final void setName(String name) {
        this.name = name;
    }

    public final String getName() {
        return this.name;
    }

    public abstract void execute(CommandSender sender, String[] args);
}
