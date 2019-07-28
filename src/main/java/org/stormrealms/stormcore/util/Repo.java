package org.stormrealms.stormcore.util;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Repo {
	public default <T> T saveImmediately(JpaRepository repo, T entity, AutowireCapableBeanFactory factory) {
		EntityManager em = factory.getBean(EntityManager.class);
		return entity;

	}
}
