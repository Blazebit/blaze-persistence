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
		
        try {
        	Class<?> hibernateSessionClass = Class.forName("org.hibernate.Session");
        	String version = em.unwrap(hibernateSessionClass).getClass().getPackage().getImplementationVersion();
        	String[] versionParts = version.split("\\.");
        	int major = Integer.parseInt(versionParts[0]);
        	
        	if (major >= 5) {
        		return new Hibernate5TransactionSynchronizationStrategy(em);
        	} else {
        		return new Hibernate4TransactionSynchronizationStrategy(em);
        	}
        } catch (ClassNotFoundException ex) {
        	throw new IllegalArgumentException("Unsupported jpa provider!", ex);
        }
    }
}
