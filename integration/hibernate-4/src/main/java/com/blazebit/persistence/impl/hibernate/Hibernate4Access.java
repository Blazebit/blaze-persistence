package com.blazebit.persistence.impl.hibernate;

import java.lang.reflect.Proxy;

import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.transaction.spi.TransactionCoordinator;
import org.hibernate.event.spi.EventSource;

import com.blazebit.apt.service.ServiceProvider;

@ServiceProvider(HibernateAccess.class)
public class Hibernate4Access implements HibernateAccess {

    @Override
    public SessionImplementor wrapSession(SessionImplementor session, boolean generatedKeys, String[][] columns, HibernateReturningResult<?> returningResult) {
        TransactionCoordinator transactionCoordinator = session.getTransactionCoordinator();
        JdbcCoordinator jdbcCoordinator = transactionCoordinator.getJdbcCoordinator();
        
        Object jdbcCoordinatorProxy = Proxy.newProxyInstance(jdbcCoordinator.getClass().getClassLoader(), new Class[]{ JdbcCoordinator.class }, new JdbcCoordinatorInvocationHandler(jdbcCoordinator, session.getFactory(), generatedKeys, columns, returningResult));
        Object transactionCoordinatorProxy = Proxy.newProxyInstance(transactionCoordinator.getClass().getClassLoader(), new Class[]{ TransactionCoordinator.class }, new Hibernate4TransactionCoordinatorInvocationHandler(transactionCoordinator, jdbcCoordinatorProxy));
        Object sessionProxy = Proxy.newProxyInstance(session.getClass().getClassLoader(), new Class[]{ SessionImplementor.class, EventSource.class }, new Hibernate4SessionInvocationHandler(session, transactionCoordinatorProxy));
        return (SessionImplementor) sessionProxy;
    }

    @Override
    public void checkTransactionSynchStatus(SessionImplementor session) {
        TransactionCoordinator coordinator = session.getTransactionCoordinator();
        coordinator.pulse();
        coordinator.getSynchronizationCallbackCoordinator().processAnyDelayedAfterCompletion();
    }

    @Override
    public void afterTransaction(SessionImplementor session, boolean success) {
        TransactionCoordinator coordinator = session.getTransactionCoordinator();
        if (!session.isTransactionInProgress() ) {
            coordinator.getJdbcCoordinator().afterTransaction();
        }
        coordinator.getSynchronizationCallbackCoordinator().processAnyDelayedAfterCompletion();
    }

}
