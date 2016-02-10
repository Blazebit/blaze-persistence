package com.blazebit.persistence.impl.hibernate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.engine.jdbc.spi.StatementPreparer;
import org.hibernate.engine.spi.SessionFactoryImplementor;


public class JdbcCoordinatorInvocationHandler implements InvocationHandler {
    
    private final JdbcCoordinator delegate;
    private final SessionFactoryImplementor sessionFactoryImplementor;
    private final boolean generated;
    private final String[][] columns;
    private final HibernateReturningResult<?> returningResult;
    private transient StatementPreparer statementPreparer;

    public JdbcCoordinatorInvocationHandler(JdbcCoordinator delegate, SessionFactoryImplementor sessionFactoryImplementor, boolean generated, String[][] columns, HibernateReturningResult<?> returningResult) {
        this.delegate = delegate;
        this.sessionFactoryImplementor = sessionFactoryImplementor;
        this.generated = generated;
        this.columns = columns;
        this.returningResult = returningResult;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("getStatementPreparer".equals(method.getName())) {
            if (statementPreparer == null) {
                statementPreparer = new StatementPreparerImpl(delegate, sessionFactoryImplementor, generated, columns, returningResult);
            }
            
            return statementPreparer;
        }
        
        return method.invoke(delegate, args);
    }

}
