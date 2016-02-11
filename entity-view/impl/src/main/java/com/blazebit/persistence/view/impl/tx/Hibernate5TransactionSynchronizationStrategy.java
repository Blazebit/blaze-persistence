package com.blazebit.persistence.view.impl.tx;

import java.lang.reflect.Method;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.transaction.Synchronization;

import com.blazebit.reflection.ExpressionUtils;
import com.blazebit.reflection.ReflectionUtils;

public class Hibernate5TransactionSynchronizationStrategy implements TransactionSynchronizationStrategy {
	
	private final EntityTransaction tx;
	private final Object synchronizationRegistry;
	private final Method registerSynchronization;

	public Hibernate5TransactionSynchronizationStrategy(EntityManager em) {
        try {
        	this.tx = em.getTransaction();
		} catch (IllegalStateException e) {
			throw new IllegalStateException("Could not access entity transaction!", e);
		}
        try {
			Object s = em.unwrap(Class.forName("org.hibernate.Session"));
			this.synchronizationRegistry = ExpressionUtils.getNullSafeValue(s, "transactionCoordinator.localSynchronizations");
			this.registerSynchronization = ReflectionUtils.getMethod(synchronizationRegistry.getClass(), "registerSynchronization", Synchronization.class);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isActive() {
		return tx.isActive();
	}

	@Override
	public void registerSynchronization(Synchronization synchronization) {
		try {
			registerSynchronization.invoke(synchronizationRegistry, synchronization);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
