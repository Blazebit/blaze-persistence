package com.blazebit.persistence.impl.hibernate;

import org.hibernate.engine.spi.SessionImplementor;


public interface HibernateAccess {

    public SessionImplementor wrapSession(SessionImplementor session, boolean generated, String[][] columns, HibernateReturningResult<?> returningResult);
    
    public void checkTransactionSynchStatus(SessionImplementor session);
    
    public void afterTransaction(SessionImplementor session, boolean success);
    
}
