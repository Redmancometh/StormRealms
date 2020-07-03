package org.stormrealms.databasing;

import java.io.Serializable;

import org.stormrealms.stormcore.Defaultable;

public interface BaseObjectManager<K extends Serializable, T extends Defaultable<?>> {
	SubDatabase<K, T> getSubDB();

	ObjectManager<K, T> getThis();

	Class<T> getType();
}
