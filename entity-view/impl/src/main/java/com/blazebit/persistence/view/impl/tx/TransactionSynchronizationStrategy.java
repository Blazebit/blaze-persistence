package com.blazebit.persistence.view.impl.tx;

import javax.transaction.Synchronization;

public interface TransactionSynchronizationStrategy {

	public boolean isActive();
	
	public void registerSynchronization(Synchronization synchronization);
	
}
