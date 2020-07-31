package org.stormrealms.stormscript.scriptable;

import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * Represents a container for registered scriptable types used to instantiate
 * new scriptable objects.
 */
@AllArgsConstructor
public class ScriptablePrototype<T extends Scriptable> {
	private @NonNull Class<T> scriptableClass;

	public T instantiate(Class<T> scriptableClass) {
		return null;
	}
}