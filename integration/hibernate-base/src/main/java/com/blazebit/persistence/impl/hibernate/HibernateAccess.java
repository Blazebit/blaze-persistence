package com.blazebit.persistence.impl.hibernate;

import org.hibernate.engine.spi.SessionImplementor;


public interface HibernateAccess {

    public Object getTransactionCoordinator(SessionImplementor session);
    
}
