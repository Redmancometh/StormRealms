package org.stormrealms.stormmobs.entity;

import java.util.concurrent.TimeUnit;

public interface Summoner
{
    public abstract long getLastSummon();

    public default long sinceLastSummon()
    {
        return (System.currentTimeMillis() - getLastSummon()) / 1000;
    }

    public default double getTimeSinceLastSummon()
    {
        return TimeUnit.MILLISECONDS.toSeconds((getLastSummon() - System.currentTimeMillis()));
    }

}
