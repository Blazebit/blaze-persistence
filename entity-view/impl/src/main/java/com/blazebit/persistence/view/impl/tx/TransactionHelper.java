package com.blazebit.persistence.view.impl.tx;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.persistence.EntityManager;
import javax.transaction.TransactionSynchronizationRegistry;

public class TransactionHelper {

    public static TransactionSynchronizationStrategy getSynchronizationStrategy(EntityManager em) {
		TransactionSynchronizationRegistry synchronizationRegistry;
        
        try {
			synchronizationRegistry = (TransactionSynchronizationRegistry) new InitialContext().lookup("java:comp/TransactionSynchronizationRegistry");
			if (synchronizationRegistry != null) {
				return new JtaTransactionSynchronizationStrategy(synchronizationRegistry);
			}
		} catch (NoInitialContextException e) {
			// Maybe in Java SE environment
			synchronizationRegistry = null;
		} catch (NamingException e) {
			throw new IllegalArgumentException("Could not access transaction synchronization registry!", e);
		}
		
		return new HibernateTransactionSynchronizationStrategy(em);
    }
}
