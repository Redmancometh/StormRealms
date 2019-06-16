package org.stormrealms.stormcore.util;

import java.util.concurrent.ConcurrentHashMap;

public class ExportSystem extends ConcurrentHashMap<String, Object> {
	private static final long serialVersionUID = 9125315152580261278L;

	@Override
	public Object getOrDefault(Object key, Object defaultValue) {
		if (this.containsKey(key)) {
			return this.get(key);
		}
		this.put((String) key, defaultValue);
		return defaultValue;
	}

}
