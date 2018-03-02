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

import com.blazebit.persistence.integration.hibernate.base.HibernateReturningResult;
import com.blazebit.persistence.spi.DbmsDialect;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.engine.jdbc.spi.StatementPreparer;
import org.hibernate.engine.spi.SessionFactoryImplementor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class JdbcCoordinatorInvocationHandler implements InvocationHandler {
    
    private final JdbcCoordinator delegate;
    private final SessionFactoryImplementor sessionFactoryImplementor;
    private final DbmsDialect dbmsDialect;
    private final String[][] columns;
    private final int[] returningSqlTypes;
    private final HibernateReturningResult<?> returningResult;
    private transient StatementPreparer statementPreparer;

    public JdbcCoordinatorInvocationHandler(JdbcCoordinator delegate, SessionFactoryImplementor sessionFactoryImplementor, DbmsDialect dbmsDialect, String[][] columns, int[] returningSqlTypes, HibernateReturningResult<?> returningResult) {
        this.delegate = delegate;
        this.sessionFactoryImplementor = sessionFactoryImplementor;
        this.dbmsDialect = dbmsDialect;
        this.columns = columns;
        this.returningSqlTypes = returningSqlTypes;
        this.returningResult = returningResult;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("getStatementPreparer".equals(method.getName())) {
            if (statementPreparer == null) {
                statementPreparer = new StatementPreparerImpl(delegate, sessionFactoryImplementor, dbmsDialect, columns, returningSqlTypes, returningResult);
            }
            
            return statementPreparer;
        }
        
        return method.invoke(delegate, args);
    }

}
