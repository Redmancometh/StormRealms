package org.stormrealms.stormmobs.entity;

import java.util.concurrent.TimeUnit;

public interface Caster
{
    public abstract long getLastSpell();

    public default long sinceLastSpell()
    {
        return (System.currentTimeMillis() - getLastSpell()) / 1000;
    }

    public default double getTimeSinceLastSpell()
    {
        return TimeUnit.MILLISECONDS.toSeconds((getLastSpell() - System.currentTimeMillis()));
    }
}
