package org.stormrealms.databasing;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.Bukkit;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.stormrealms.exceptions.ObjectNotPresentException;
import org.stormrealms.stormcore.Defaultable;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormcore.util.SpecialFuture;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * @param <V> This is the type of object persisted
 * @param <K> This is the type for the key which is used to lookup the object in
 *            both the cache and database.
 * @author Redmancometh
 */
public class SubDatabase<K extends Serializable, V extends Defaultable> {
	private final Class<V> type;
	private SessionFactory factory;
	public Function<K, V> defaultObjectBuilder;
	LoadingCache<K, SpecialFuture<V>> cache = CacheBuilder.newBuilder().build(new CacheLoader<K, SpecialFuture<V>>() {
		@Override
		public SpecialFuture<V> load(K key) {
			return SpecialFuture.supplyAsync(() -> {
				try (Session session = factory.openSession()) {
					V result = session.get(type, key);
					if (result == null)
						return defaultObjectBuilder.apply(key);
					return result;
				} catch (SecurityException | IllegalArgumentException e) {
					SpecialFuture
							.runSync(() -> Bukkit.getLogger().log(Level.SEVERE, "Failed to get database object", e));
					throw new RuntimeException(e);
				}
			});
		}
	});

	public SubDatabase(Class<V> type, SessionFactory factory) {
		super();
		this.factory = factory;
		this.type = type;
		this.defaultObjectBuilder = (key) -> {
			try {
				V v = (V) type.getConstructors()[0].newInstance(new Object[0]);
				v.setDefaults(key);
				return v;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | SecurityException e) {
				e.printStackTrace();
			}
			return null;
		};
	}

	public void deleteObject(V e) {
		try (Session s = factory.openSession()) {
			s.delete(e);
		}
	}

	/**
	 * This method is an insta-return which assumes the value is already loaded into
	 * the cache.
	 *
	 * @param key
	 * @return
	 */
	public V get(K key) {
		SpecialFuture<V> future = cache.asMap().get(key);
		if (future == null)
			System.out.println("DAFUQ");
		return future.get();
	}

	public SpecialFuture<List<V>> topX(int x, String onProperty) {
		return queryWithCriteria((criteria) -> {
			criteria.addOrder(Order.desc(onProperty));
			criteria.setFirstResult(0);
			criteria.setMaxResults(50);
		});
	}

	@SuppressWarnings("deprecation")
	public SpecialFuture<List<V>> queryWithCriteria(Consumer<Criteria> criteriaCallback) {
		return SpecialFuture.supplyAsync(() -> {
			List<V> list;
			try (Session session = factory.openSession()) {
				Criteria c = session.createCriteria(type);
				criteriaCallback.accept(c);
				list = c.list();
			}
			return list;
		});
	}

	public SessionFactory getFactory() {
		return factory;
	}

	public Class<V> getMyType() {
		return this.type;
	}

	/**
	 * Get an object from the db async using CompletableFuture. Call this with
	 * .thenAccept or something.
	 *
	 * @param e
	 * @return
	 */
	public SpecialFuture<V> getObject(K e) {
		try {
			return cache.get(e);
		} catch (ExecutionException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	public SpecialFuture<V> getWithCriteria(K e, Criteria... criteria) {
		try {
			return cache.get(e);
		} catch (ExecutionException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	public void insertObject(K key, V value) {
		System.out.println("Inserting: " + value + " AT: " + key);
		cache.asMap().put(key, SpecialFuture.supplyAsync(() -> value));
	}

	public Session newSession() {
		return factory.openSession();
	}

	/**
	 * Purge an object by using it's KEY object. Warning: This is unchecked, and the
	 * object will not be saved.
	 *
	 * @param e
	 */
	public void purgeObject(K e) {
		cache.asMap().remove(e);
	}

	/**
	 * Save a value and purge it from the cache.
	 *
	 * @param e
	 * @return
	 * @throws ObjectNotPresentException
	 */
	public SpecialFuture<?> saveAndPurge(V e, K uuid) throws ObjectNotPresentException {
		return saveObject(e).thenRun(() -> cache.asMap().remove(uuid));
	}

	/**
	 * This is ONLY saved if the key is found in the cache!
	 *
	 * @param e
	 * @return
	 * @throws ObjectNotPresentException
	 */
	@Deprecated
	public CompletableFuture<Void> saveFromKey(K e) throws ObjectNotPresentException {
		if (!cache.asMap().containsKey(e))
			throw new ObjectNotPresentException(e.toString(), type);
		return CompletableFuture.runAsync(() -> {
			try (Session session = factory.openSession()) {
				try {
					session.beginTransaction();
					cache.get(e).thenAccept(session::saveOrUpdate);
					session.getTransaction().commit();
					session.flush();
				} catch (Exception e2) {
					e2.printStackTrace();
				}

			}
		}, StormCore.getInstance().getPool());

	}

	/**
	 * Save an object without purging it
	 *
	 * @param e
	 * @return
	 */
	public SpecialFuture<?> saveObject(V e) {
		return SpecialFuture.runAsync(() -> {
			try (Session session = factory.openSession()) {
				Object e2 = session.merge(e);
				session.beginTransaction();
				session.saveOrUpdate(e2);
				session.getTransaction().commit();
				session.close();
			}
		});
	}

}
