package org.stormrealms.stormcore.databasing;

import java.io.Serializable;
import java.util.List;

import org.stormrealms.stormcore.Defaultable;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormcore.exceptions.ObjectNotPresentException;
import org.stormrealms.stormcore.util.SpecialFuture;

public class ObjectManager<K extends Serializable, T extends Defaultable<K>> implements BaseObjectManager<K, T> {
	private final Class<T> type;

	public ObjectManager(Class<T> type) {
		this.type = type;
	}

	/**
	 * Deletes a record
	 * 
	 * @param e
	 */
	public void delete(T e) {
		getSubDB().deleteObject(e);
	}

	/**
	 * Returns the top X players based on the property given
	 * 
	 * @param x          This is how many records to return
	 * @param onProperty This is the column name. When you use @Column(name="name")
	 *                   it's called "name." Always use
	 * @Column and explicitly set the name if you intend on using this.
	 * @return
	 */
	public SpecialFuture<List<T>> topX(int x, String onProperty) {
		return getSubDB().topX(x, onProperty);
	}

	/**
	 * Don't use this if you can avoid.
	 *
	 * @return
	 */
	public SubDatabase<K, T> getSubDB() {
		return StormCore.getInstance().getMasterDB().getSubDBForType(type);
	}

	@Override
	public ObjectManager<K, T> getThis() {
		return this;
	}

	/**
	 * Get the type that's being mapped by this ObjectManager
	 */
	public Class<T> getType() {
		return type;
	}

	/**
	 * Wait for completion of the SpecialFuture Synchronously
	 * 
	 * @param key
	 * @return
	 */
	public T getBlocking(K key) {
		return getSubDB().get(key);
	}

	/**
	 * Get a record for the player with this uuid
	 * 
	 * @param uuid
	 * @return
	 */
	public SpecialFuture<T> getRecord(K uuid) {
		return getSubDB().getObject(uuid);
	}

	/**
	 * Insert an object into the subDatabase for future persistence
	 * 
	 * @param key
	 * @param value
	 */
	public void insertObject(K key, T value) {
		getSubDB().insertObject(key, value);
	}

	/**
	 * Save without purging
	 * 
	 * @param e
	 * @return
	 */
	public SpecialFuture<?> save(T e) {
		return StormCore.getInstance().getMasterDB().getSubDBForType(type).saveObject(e);
	}

	/**
	 * Save without purging
	 * 
	 * @param uuid
	 * @return
	 */
	public SpecialFuture<T> save(K uuid) {
		return getSubDB().getObject(uuid).thenAccept((record) -> getSubDB().saveObject(record));
	}

	/**
	 * Save an object and purge it from the cache
	 * 
	 * @param e
	 * @param uuid
	 * @return
	 */
	public SpecialFuture<?> saveAndPurge(T e, K uuid) {
		try {
			return getSubDB().saveAndPurge(e, uuid);
		} catch (ObjectNotPresentException ex) {
			ex.printStackTrace();
		}
		return null;
	}
}
