/*
 * Copyright 2014 - 2022 Blazebit.
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

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.integration.hibernate.base.HibernateAccess;
import com.blazebit.persistence.integration.hibernate.base.HibernateReturningResult;
import com.blazebit.persistence.spi.DbmsDialect;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.sql.exec.spi.ExecutionContext;

import java.lang.reflect.Proxy;

/**
 * @author Christian Beikov
 * @since 1.6.7
 */
@ServiceProvider(HibernateAccess.class)
public class Hibernate60Access implements HibernateAccess {

    @Override
    public ExecutionContext wrapExecutionContext(ExecutionContext executionContext, DbmsDialect dbmsDialect, String[][] returningColumns, int[] returningColumnTypes, HibernateReturningResult<Object[]> returningResult) {
        SharedSessionContractImplementor session = executionContext.getSession();
        JdbcCoordinator jdbcCoordinator = session.getJdbcCoordinator();

        Object jdbcCoordinatorProxy = Proxy.newProxyInstance(jdbcCoordinator.getClass().getClassLoader(), new Class[]{JdbcCoordinator.class}, new JdbcCoordinatorInvocationHandler(jdbcCoordinator, session.getFactory(), dbmsDialect, returningColumnTypes, returningResult));
        Object sessionProxy = Proxy.newProxyInstance(session.getClass().getClassLoader(), new Class[]{SessionImplementor.class, EventSource.class}, new Hibernate60SessionInvocationHandler(session, jdbcCoordinatorProxy));
        Object exceutionContextProxy = Proxy.newProxyInstance(session.getClass().getClassLoader(), new Class[]{ ExecutionContext.class }, new Hibernate60ExecutionContextInvocationHandler(executionContext, sessionProxy));
        return (ExecutionContext) exceutionContextProxy;
    }

}
