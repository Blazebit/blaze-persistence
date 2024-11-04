/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate;

import org.hibernate.engine.jdbc.spi.StatementPreparer;
import org.hibernate.engine.spi.SessionImplementor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.3.0
 */
public class SecondaryTableUpdateSupportingPreparedStatementInvocationHandler implements InvocationHandler {

    private final SessionImplementor session;
    private final StatementPreparer statementPreparer;
    private final PreparedStatement insertStatement;
    private final String[] updates;
    private final String[] inserts;
    private final List<Map.Entry<Method, Object[]>> parameters = new ArrayList<>();
    private PreparedStatement preparedStatement;
    private int index;
    private int insertCount;

    public SecondaryTableUpdateSupportingPreparedStatementInvocationHandler(SessionImplementor session, StatementPreparer statementPreparer, PreparedStatement insertStatement, String[] updates, String[] inserts) {
        this.session = session;
        this.statementPreparer = statementPreparer;
        this.insertStatement = insertStatement;
        this.updates = updates;
        this.inserts = inserts;
    }

    public void prepareNext() {
        preparedStatement = null;
        parameters.clear();
        while (index < updates.length) {
            String updateSql = updates[index++];
            if (updateSql != null) {
                preparedStatement = statementPreparer.prepareStatement(updateSql, false);
                break;
            }
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        PreparedStatement s;
        if (preparedStatement == null) {
            s = insertStatement;
        } else {
            s = preparedStatement;
        }
        if ("executeUpdate".equals(method.getName()) && method.getParameterTypes().length == 0) {
            if (preparedStatement == null) {
                return insertCount = insertStatement.executeUpdate();
            } else {
                int updateRows = preparedStatement.executeUpdate();
                if (updateRows != insertCount) {
                    // This is where our secondary table insert comes in
                    PreparedStatement ps = null;
                    int i = index - 1;
                    try {
                        ps = statementPreparer.prepareStatement(inserts[i], false);
                        for (Map.Entry<Method, Object[]> entry : parameters) {
                            entry.getKey().invoke(ps, entry.getValue());
                        }
                        session.getTransactionCoordinator().getJdbcCoordinator().getResultSetReturn().executeUpdate(ps);
                    } finally {
                        if (ps != null) {
                            session.getTransactionCoordinator().getJdbcCoordinator().release(ps);
                        }
                    }
                }
                return 0;
            }
        }
        if (method.getName().startsWith("set")) {
            parameters.add(new AbstractMap.SimpleEntry<>(method, args));
        }
        return method.invoke(s, args);
    }

}
