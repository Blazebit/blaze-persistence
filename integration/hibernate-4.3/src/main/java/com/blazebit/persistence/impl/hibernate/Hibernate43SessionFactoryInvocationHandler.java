package com.blazebit.persistence.impl.hibernate;

import org.hibernate.engine.spi.SessionFactoryImplementor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;


public class Hibernate43SessionFactoryInvocationHandler implements InvocationHandler {

    private final SessionFactoryImplementor delegate;
    private final Object dialect;

    public Hibernate43SessionFactoryInvocationHandler(SessionFactoryImplementor delegate, Object dialect) {
        this.delegate = delegate;
        this.dialect = dialect;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("getDialect".equals(method.getName())) {
            return dialect;
        }
        
        return method.invoke(delegate, args);
    }

}
