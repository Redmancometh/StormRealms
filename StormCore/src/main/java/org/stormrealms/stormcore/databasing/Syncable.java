package org.stormrealms.stormcore.databasing;

import org.bukkit.entity.Player;
import org.stormrealms.stormcore.Defaultable;

public interface Syncable<T> extends Defaultable<T> {
	public default void trySync(T key) {
		Player p = getPlayerFromId(key);
		if (p != null)
			sync(key);
	}

	public default void tryUpdateLocal(T key) {
		Player p = getPlayerFromId(key);
		if (p != null)
			updateFromLocal(key);
	}

	public abstract Player getPlayerFromId(T id);

	public abstract void sync(T key);

	public abstract void updateFromLocal(T key);
}
