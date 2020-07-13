package org.stormrealms.stormcore.magic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.stereotype.Component;

@Component
public class EventScope implements Scope {
	private Map<String, Object> objectMap = Collections.synchronizedMap(new HashMap<String, Object>());
	private volatile boolean scopeOpen = false;

	public EventScope() {
		System.out.println(getClass().getName() + ": Creating scope");
	}

	/**
	 * Mark the begining of the scope, before the scope is opened no beans are
	 * registered.
	 */
	public void openScope() {
		scopeOpen = true;
		System.out.println(getClass().getName() + ": Opened the scope");
	}

	/**
	 * Mark the end of the scope, after the scope is closed, no beans are
	 * registered.
	 */
	public void closeScope() {
		scopeOpen = false;
		clear();
		System.out.println(getClass().getName() + ": Closed the scope");
	}

	@Override
	public Object get(String name, ObjectFactory<?> objectFactory) {
		if (scopeOpen && !objectMap.containsKey(name)) {
			// Only add the bean if the scope is open and the bean is not yet already
			// registered
			objectMap.put(name, objectFactory.getObject());
		}
		return objectMap.get(name);
	}

	@Override
	public Object remove(String name) {
		return objectMap.remove(name);
	}

	@Override
	public void registerDestructionCallback(String name, Runnable callback) {
		// do nothing
	}

	@Override
	public Object resolveContextualObject(String key) {
		return null;
	}

	/**
	 * Clear the cached beans in the scopes backing store
	 */
	private void clear() {
		objectMap.clear();
		System.out.println(getClass().getName() + ": Clear scope");
	}

	@Override
	public String getConversationId() {
		return "Dialog";
	}
}