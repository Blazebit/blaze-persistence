/*
 * Copyright 2014 - 2023 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

import jakarta.persistence.EntityManager;

/**
 * @author Christian Beikov
 * @author Eugen Mayer
 * @since 1.6.9
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
