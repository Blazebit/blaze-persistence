/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.integration.hibernate;

import org.hibernate.engine.spi.SessionFactoryImplementor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
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
