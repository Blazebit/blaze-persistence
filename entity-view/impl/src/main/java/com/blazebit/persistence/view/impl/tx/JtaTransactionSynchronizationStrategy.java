package com.blazebit.persistence.view.impl.tx;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;

public class JtaTransactionSynchronizationStrategy implements TransactionSynchronizationStrategy {
	
	private final TransactionSynchronizationRegistry synchronizationRegistry;

	public JtaTransactionSynchronizationStrategy(TransactionSynchronizationRegistry synchronizationRegistry) {
		this.synchronizationRegistry = synchronizationRegistry;
	}

	@Override
	public boolean isActive() {
		return synchronizationRegistry.getTransactionStatus() == Status.STATUS_ACTIVE;
	}

	@Override
	public void registerSynchronization(Synchronization synchronization) {
		synchronizationRegistry.registerInterposedSynchronization(synchronization);
	}

}
