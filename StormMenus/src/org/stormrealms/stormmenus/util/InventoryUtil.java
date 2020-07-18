package org.stormrealms.stormmenus.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil
{
    
    @Deprecated
    public static void takeOne(ItemStack i, Player p)
    {
        for (ItemStack item : p.getInventory())
        {
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName())
            {
                String name = item.getItemMeta().getDisplayName();
                if (name.equals(i.getItemMeta().getDisplayName()))
                {
                    if (item.getAmount() > 1)
                    {
                        item.setAmount(item.getAmount() - 1);
                        return;
                    }
                    p.getInventory().removeItem(i);
                    return;
                }

            }
        }
    }
}
