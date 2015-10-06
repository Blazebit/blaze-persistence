package com.blazebit.persistence.impl.hibernate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.hibernate.engine.spi.SessionImplementor;


public class Hibernate5SessionInvocationHandler implements InvocationHandler {
    
    private final SessionImplementor delegate;
    private final Object jdbcCoordinatorProxy;

    public Hibernate5SessionInvocationHandler(SessionImplementor delegate, Object jdbcCoordinatorProxy) {
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
