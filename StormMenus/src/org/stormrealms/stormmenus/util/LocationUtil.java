package org.stormrealms.stormmenus.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class LocationUtil
{
    public static Location getBlockBehind(Location loc)
    {
        String playerDirection;
        float yaw = loc.getYaw();
        World w = loc.getWorld();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        if (yaw < 0)
        {
            yaw = yaw + 360;
        }

        if ((yaw >= 315) && (yaw <= 360))
        {
            playerDirection = "south";
        } else if ((yaw >= 0) && (yaw <= 45))
        {
            playerDirection = "south";
        } else if ((yaw >= 45) && (yaw <= 135))
        {
            playerDirection = "west";
        } else if ((yaw >= 135) && (yaw <= 180))
        {
            playerDirection = "north";
        } else if ((yaw >= 180) && (yaw <= 225))
        {
            playerDirection = "north";
        } else if ((yaw >= 225) && (yaw <= 315))
        {
            playerDirection = "east";
        } else
        {
            playerDirection = "east";
        }

        if (playerDirection == "north")
        {
            z = z + 1;
        } else if (playerDirection == "east")
        {
            x = x - 1;
        } else if (playerDirection == "south")
        {
            z = z - 1;
        } else if (playerDirection == "west")
        {
            x = x + 1;
        }
        Block b = w.getBlockAt(x, y, z);
        //todo have consumer<block> to manipulate this as well as returning location or something
        return b.getLocation();
    }

    public static Location getBlockDirectlyBehindPlayer(Player player)
    {

        String playerDirection;
        float yaw = player.getLocation().getYaw();
        World w = player.getWorld();
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();

        if (yaw < 0)
        {
            yaw = yaw + 360;
        }

        if ((yaw >= 315) && (yaw <= 360))
        {
            playerDirection = "south";
        } else if ((yaw >= 0) && (yaw <= 45))
        {
            playerDirection = "south";
        } else if ((yaw >= 45) && (yaw <= 135))
        {
            playerDirection = "west";
        } else if ((yaw >= 135) && (yaw <= 180))
        {
            playerDirection = "north";
        } else if ((yaw >= 180) && (yaw <= 225))
        {
            playerDirection = "north";
        } else if ((yaw >= 225) && (yaw <= 315))
        {
            playerDirection = "east";
        } else
        {
            playerDirection = "east";
        }

        if (playerDirection == "north")
        {
            z = z + 1;
        } else if (playerDirection == "east")
        {
            x = x - 1;
        } else if (playerDirection == "south")
        {
            z = z - 1;
        } else if (playerDirection == "west")
        {
            x = x + 1;
        }
        Block b = w.getBlockAt(x, y, z);
        //todo have consumer<block> to manipulate this as well as returning location or something
        return b.getLocation();
    }
}
