package org.stormrealms.stormmenus.util;

import java.io.Serializable;

public interface Initializable<T extends Serializable> {
	public abstract void initialize(T e);
}
