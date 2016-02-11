package com.blazebit.persistence.impl.hibernate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.hibernate.engine.spi.SessionImplementor;


public class Hibernate4SessionInvocationHandler implements InvocationHandler {
    
    private final SessionImplementor delegate;
    private final Object transactionCoordinatorProxy;

    public Hibernate4SessionInvocationHandler(SessionImplementor delegate, Object transactionCoordinatorProxy) {
        this.delegate = delegate;
        this.transactionCoordinatorProxy = transactionCoordinatorProxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("getTransactionCoordinator".equals(method.getName())) {
            return transactionCoordinatorProxy;
        }
        
        return method.invoke(delegate, args);
    }

}
