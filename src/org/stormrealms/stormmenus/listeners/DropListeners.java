package org.stormrealms.stormmenus.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

public class DropListeners
{
    @EventHandler
    public void onDeath(EntityDeathEvent e)
    {
        if (e.getEntity() instanceof Player) return;

    }
}
