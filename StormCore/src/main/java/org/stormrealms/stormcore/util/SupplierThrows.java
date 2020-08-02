package org.stormrealms.stormcore.util;

public interface SupplierThrows<T, E extends Throwable> {
	T get() throws E;
}