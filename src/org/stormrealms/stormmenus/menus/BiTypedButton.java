package org.stormrealms.stormmenus.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.stormrealms.stormmenus.absraction.BaseMenu;
import org.stormrealms.stormmenus.util.QuadConsumer;
import org.stormrealms.stormmenus.util.TriFunction;

public class BiTypedButton<T, U> extends BaseTypedMenuButton
{
    private TriFunction<Player, T, U, ItemStack> buttonConstructor;
    private QuadConsumer<ClickType, T, U, Player> clickAction;

    public BiTypedButton()
    {

    }

    public BiTypedButton(TriFunction<Player, T, U, ItemStack> buttonConstructor)
    {
        this.buttonConstructor = buttonConstructor;
    }

    public BiTypedButton(TriFunction<Player, T, U, ItemStack> buttonConstructor, QuadConsumer<ClickType, T, U, Player> clickAction)
    {
        this.buttonConstructor = buttonConstructor;
        this.clickAction = clickAction;

    }

    public void setAction(QuadConsumer<ClickType, T, U, Player> action)
    {
        this.clickAction = action;
    }

    public ItemStack constructButton(T t, U u, BaseMenu menu, Player p)
    {
        return processPlaceholders(buttonConstructor.apply(p, t, u), menu, p);
    }

    public QuadConsumer<ClickType, T, U, Player> getClickAction()
    {
        return clickAction;
    }

    public void setClickAction(QuadConsumer<ClickType, T, U, Player> clickAction)
    {
        this.clickAction = clickAction;
    }
}
