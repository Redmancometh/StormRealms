package org.stormrealms.mediators;

import com.redmancometh.redcore.Defaultable;
import com.redmancometh.redcore.databasing.SubDatabase;

import java.io.Serializable;

public interface BaseObjectManager<K extends Serializable, T extends Defaultable<?>> {
	SubDatabase<K, T> getSubDB();

	ObjectManager<K, T> getThis();

	Class<T> getType();
}
