package org.stormrealms.stormcore.databasing;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Serializable;
import java.lang.reflect.Modifier;

import org.stormrealms.stormcore.util.SpecialFuture;

public abstract class XComManager<K extends Serializable, T extends Syncable<K>> extends ObjectManager<K, T> {

	protected String key;
	public Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.PROTECTED)
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES).create();

	public abstract void publish(T e);

	public abstract T getFromCache(K uuid);

	public XComManager(Class<T> type) {
		super(type);
	}

	protected String json(T e) {
		return gson.toJson(e);
	}

	@Override
	public SpecialFuture<?> saveAndPurge(T e, K uuid) {
		SpecialFuture.runAsync(() -> publish(e));
		return super.saveAndPurge(e, uuid);
	}

	@Override
	public SpecialFuture<?> save(T e) {
		publish(e);
		return super.save(e);
	}

	@Override
	public SpecialFuture<T> save(K key) {
		return getRecord(key).thenAccept((record) -> publish(record)).thenRun(() -> super.save(key));
	}

	@Override
	public SpecialFuture<T> getRecord(K uuid) {
		T e = getFromCache(uuid);
		if (e != null) {
			super.insertObject(uuid, e);
			return SpecialFuture.supplyAsync(() -> e);
		}
		return super.getRecord(uuid);
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
