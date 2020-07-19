package org.stormrealms.stormcore.databasing;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import redis.clients.jedis.Jedis;
import org.stormrealms.stormcore.databasing.Syncable;

public class JedisObjectManager<K extends Serializable, T extends Syncable<K>> extends XComManager<K, T> {

	/**
	 * Renamed to this long as shit, so I don't have to keep confusing jedis. and
	 * jedis().
	 */
	private Jedis internalJedisInstance;
	@Getter
	@Setter
	private String redisAddress;

	public JedisObjectManager(Class<T> type, String redisAddress) {
		super(type);
		this.redisAddress = redisAddress;
		jedis();
	}

	public Jedis jedis() {
		if (internalJedisInstance == null)
			return new Jedis(redisAddress);
		return internalJedisInstance;
	}

	@Override
	public void publish(T e) {
		jedis().set(getKey() + "-" + e.toString().toString(), json(e));
		System.out.println(json(e));
	}

	@Override
	public T getFromCache(K uuid) {
		String rawJson = jedis().get(getKey() + "-" + uuid.toString());
		System.out.println("Json: " + rawJson);
		if (rawJson == null)
			return null;
		T e = gson.fromJson(rawJson, getType());
		jedis().del(getKey() + "-" + uuid.toString());
		return e;
	}

}
