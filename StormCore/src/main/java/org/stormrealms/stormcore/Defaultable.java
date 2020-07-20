package org.stormrealms.stormcore;

import java.lang.reflect.Modifier;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public interface Defaultable<K> {
	Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.PROTECTED)
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES).create();
	
	K getKey();

	void setDefaults(K e);

	default String toJsonString() {
		return gson.toJson(this);
	}
}
