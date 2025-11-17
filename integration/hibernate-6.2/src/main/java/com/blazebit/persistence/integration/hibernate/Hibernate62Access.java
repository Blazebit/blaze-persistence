/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate;

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
public class Hibernate62Access implements HibernateAccess {

    @Override
    public ExecutionContext wrapExecutionContext(ExecutionContext executionContext, DbmsDialect dbmsDialect, String[][] returningColumns, int[] returningColumnTypes, HibernateReturningResult<Object[]> returningResult) {
        SharedSessionContractImplementor session = executionContext.getSession();
        JdbcCoordinator jdbcCoordinator = session.getJdbcCoordinator();

        Object jdbcCoordinatorProxy = Proxy.newProxyInstance(jdbcCoordinator.getClass().getClassLoader(), new Class[]{JdbcCoordinator.class}, new JdbcCoordinatorInvocationHandler(jdbcCoordinator, session.getFactory(), dbmsDialect, returningColumnTypes, returningResult));
        Object sessionProxy = Proxy.newProxyInstance(session.getClass().getClassLoader(), new Class[]{SessionImplementor.class, EventSource.class}, new Hibernate62SessionInvocationHandler( session, jdbcCoordinatorProxy));
        Object exceutionContextProxy = Proxy.newProxyInstance(session.getClass().getClassLoader(), new Class[]{ ExecutionContext.class }, new Hibernate62ExecutionContextInvocationHandler( executionContext, sessionProxy));
        return (ExecutionContext) exceutionContextProxy;
    }

}
