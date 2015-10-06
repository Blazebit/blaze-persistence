package com.blazebit.persistence.impl.hibernate;

import org.hibernate.engine.spi.SessionImplementor;

import com.blazebit.apt.service.ServiceProvider;

@ServiceProvider(HibernateAccess.class)
public class Hibernate5Access implements HibernateAccess {

    @Override
    public Object getTransactionCoordinator(SessionImplementor session) {
        return session.getTransactionCoordinator();
    }

}
