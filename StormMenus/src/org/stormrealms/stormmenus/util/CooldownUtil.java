package org.stormrealms.stormmenus.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CooldownUtil
{
    /**
     * TODO: implement custom LoadingCache, and custom CacheBuilder specifically for cooldowns.
     */

    /**
     * @param l
     * @return
     */
    public static LoadingCache buildCooldownCache(long l)
    {
        return CacheBuilder.newBuilder().expireAfterWrite(l, TimeUnit.SECONDS).build(new CacheLoader<UUID, Boolean>()
        {
            @Override
            public Boolean load(UUID key)
            {
                return false;
            }
        });
    }

    public static LoadingCache buildCooldownCache(TimeUnit units, int seconds)
    {
        return CacheBuilder.newBuilder().expireAfterWrite(seconds, units).build(new CacheLoader<UUID, Boolean>()
        {
            @Override
            public Boolean load(UUID key)
            {
                return false;
            }
        });
    }
}
