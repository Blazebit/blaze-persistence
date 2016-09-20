package com.blazebit.persistence.impl.hibernate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;


public class Hibernate43TransactionCoordinatorInvocationHandler implements InvocationHandler {
    
    private final Object delegate;
    private final Object jdbcCoordinatorProxy;

    public Hibernate43TransactionCoordinatorInvocationHandler(Object delegate, Object jdbcCoordinatorProxy) {
        this.delegate = delegate;
        this.jdbcCoordinatorProxy = jdbcCoordinatorProxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("getJdbcCoordinator".equals(method.getName())) {
            return jdbcCoordinatorProxy;
        }
        
        return method.invoke(delegate, args);
    }

}
