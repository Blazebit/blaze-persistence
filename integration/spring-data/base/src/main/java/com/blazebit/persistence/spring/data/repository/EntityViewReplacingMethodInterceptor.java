/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.repository;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.spi.type.BasicDirtyTracker;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.support.RepositoryProxyPostProcessor;

import javax.persistence.EntityManager;

/**
 * @author Christian Beikov
 * @since 1.3.0
 */
public class EntityViewReplacingMethodInterceptor implements MethodInterceptor, RepositoryProxyPostProcessor {

    private final EntityManager em;
    private final EntityViewManager evm;

    public EntityViewReplacingMethodInterceptor(EntityManager em, EntityViewManager evm) {
        this.em = em;
        this.evm = evm;
    }

    @Override
    public void postProcess(ProxyFactory factory, RepositoryInformation repositoryInformation) {
        factory.addAdvice(this);
    }

    @Override
    public Object invoke(@SuppressWarnings("null") MethodInvocation invocation) throws Throwable {
        Object o = invocation.proceed();

        if (invocation.getMethod().getName().startsWith("save")) {
            Object[] arguments = invocation.getArguments();
            if (arguments.length == 1) {
                arguments[0] = convertToEntity(arguments[0]);
            } else {
                return convertToEntity(o);
            }
        }

        return o;
    }

    private Object convertToEntity(Object entityOrView) {
        if (entityOrView instanceof BasicDirtyTracker) {
            EntityViewProxy view = (EntityViewProxy) entityOrView;
            return evm.getEntityReference(em, view);
        }
        return entityOrView;
    }
}
