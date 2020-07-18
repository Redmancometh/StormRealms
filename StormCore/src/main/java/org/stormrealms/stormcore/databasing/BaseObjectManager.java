package org.stormrealms.stormcore.databasing;

import java.io.Serializable;

import org.stormrealms.stormcore.Defaultable;

public interface BaseObjectManager<K extends Serializable, T extends Defaultable<K>> {
	SubDatabase<K, T> getSubDB();

	ObjectManager<K, T> getThis();

	Class<T> getType();
}
