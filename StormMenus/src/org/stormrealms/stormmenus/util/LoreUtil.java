package org.stormrealms.stormmenus.util;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class LoreUtil
{
    public static List<String> colorizeLore(List<String> ogLore)
    {
        List<String> newLore = new ArrayList();
        for (int x = 0; x < ogLore.size(); x++)
            newLore.add(x, ChatColor.translateAlternateColorCodes('&', ogLore.get(x)));
        return newLore;
    }

    public static List<String> replaceAll(List<String> input, String toReplace, String replaceWith)
    {
        for (final ListIterator<String> i = input.listIterator(); i.hasNext();)
        {
            final String element = i.next();
            i.set(element.replace(toReplace, replaceWith));
        }
        return input;
    }

}
