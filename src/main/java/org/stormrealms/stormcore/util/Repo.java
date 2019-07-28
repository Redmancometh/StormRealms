package org.stormrealms.stormcore.util;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.bukkit.Bukkit;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.stormrealms.exceptions.ObjectNotPresentException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class Repo<K extends Serializable, V extends Initializable> {
	@PersistenceContext
	private EntityManager em;
	private Class<V> type;
	public Function<K, V> defaultObjectBuilder;

	@SuppressWarnings("deprecation")
	public Repo(Class<V> type) {
		super();
		this.type = type;
		this.defaultObjectBuilder = (key) -> {
			try {
				V v = type.newInstance();
				v.initialize(key);
				return v;
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
			return null;
		};
	}

	LoadingCache<K, SpecialFuture<V>> cache = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.MINUTES)
			.build(new CacheLoader<K, SpecialFuture<V>>() {
				@Override
				public SpecialFuture<V> load(K key) {
					return SpecialFuture.supplyAsync(() -> {
						try (Session session = em.unwrap(SessionFactory.class).openSession()) {
							V result = session.get(type, key);
							if (result == null)
								return defaultObjectBuilder.apply(key);
							return result;
						} catch (SecurityException | IllegalArgumentException e) {
							SpecialFuture.runSync(
									() -> Bukkit.getLogger().log(Level.SEVERE, "Failed to get database object", e));
							throw new RuntimeException(e);
						}
					});
				}
			});

	public void deleteObject(V e) {
		try (Session s = em.unwrap(SessionFactory.class).openSession()) {
			s.delete(e);
		}
	}

	/**
	 * This method is an insta-return which assumes the value is already loaded into
	 * the cache.
	 *
	 * @param e
	 * @return
	 */
	public V get(K e) {
		SpecialFuture<V> future = cache.asMap().get(e);
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
			try (Session session = em.unwrap(SessionFactory.class).openSession()) {
				Criteria c = session.createCriteria(type);
				criteriaCallback.accept(c);
				list = c.list();
			}
			return list;
		});
	}

	public SessionFactory getFactory() {
		return em.unwrap(SessionFactory.class);
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
		return em.unwrap(SessionFactory.class).openSession();
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
	 * Open a transaction, call the supplier, complete the transaction. This is a
	 * wrapper for Springs garbage transaction handling
	 * 
	 * @param e
	 * @param k
	 * @param supplier
	 * @return
	 */
	public SpecialFuture<?> saveOrUpdate(V v, Supplier supplier) {
		return SpecialFuture.runAsync(() -> {
			try (Session session = em.unwrap(SessionFactory.class).openSession()) {
				session.beginTransaction();
				supplier.get();
				session.getTransaction().commit();
			}
		});
	}

	/**
	 * Open a transaction, call the supplier, complete the transaction. This is a
	 * wrapper for Springs garbage transaction handling
	 * 
	 * @param e
	 * @param k
	 * @param supplier
	 * @return
	 */
	public SpecialFuture<?> get(K k, Supplier supplier) {
		return SpecialFuture.runAsync(() -> {
			try (Session session = em.unwrap(SessionFactory.class).openSession()) {
				session.beginTransaction();
				supplier.get();
				session.getTransaction().commit();
			}
		});
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
	 * Save an object without purging it
	 *
	 * @param e
	 * @return
	 */
	public SpecialFuture<?> saveObject(V e) {
		return SpecialFuture.runAsync(() -> {
			try (Session session = em.unwrap(SessionFactory.class).openSession()) {
				session.beginTransaction();
				session.saveOrUpdate(e);
				session.getTransaction().commit();
			}
		});
	}

}
