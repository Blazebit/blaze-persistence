/*
 * Copyright 2014 - 2019 Blazebit.
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

import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.hql.internal.ast.HqlSqlWalker;
import org.hibernate.hql.internal.ast.tree.AssignmentSpecification;
import org.hibernate.hql.internal.ast.tree.FromElement;
import org.hibernate.hql.internal.ast.tree.UpdateStatement;
import org.hibernate.hql.spi.id.MultiTableBulkIdStrategy;
import org.hibernate.hql.spi.id.TableBasedUpdateHandlerImpl;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.persister.entity.Queryable;

import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.3.0
 */
public class CustomTableBasedUpdateHandlerImpl implements MultiTableBulkIdStrategy.UpdateHandler {

    private final TableBasedUpdateHandlerImpl delegate;
    private final String[] secondaryTableUpdates;
    private final String[] secondaryTableInserts;

    public CustomTableBasedUpdateHandlerImpl(TableBasedUpdateHandlerImpl delegate, HqlSqlWalker walker) {
        this.delegate = delegate;
        // Here comes the madness...
        // We need to somehow execute our code within the temporary table scope, but since there is no way for use to do this externally,
        // we need to use invocation handlers to get a callback at the right time
        // First, we reduce the update statements
        String[] updates = delegate.getSqlStatements();
        UpdateStatement updateStatement = (UpdateStatement) walker.getAST();
        FromElement fromElement = updateStatement.getFromClause().getFromElement();
        AbstractEntityPersister targetedPersister = (AbstractEntityPersister) fromElement.getQueryable();
        String[] tableNames = targetedPersister.getConstraintOrderedTableNameClosure();
        String[][] columnNames = targetedPersister.getContraintOrderedTableKeyColumnClosure();

        int subclassCount = delegate.getTargetedQueryable().getEntityMetamodel().getSubclassEntityNames().size();
        Set<String> subclassTableNames = new HashSet<>();
        for (int i = 0; i < subclassCount; i++) {
            subclassTableNames.add(delegate.getTargetedQueryable().getSubclassTableName(i));
        }

        final String[] secondaryTableUpdates = updates.clone();
        final String[] secondaryTableInserts = new String[updates.length];

        StringBuilder sb = new StringBuilder();
        StringBuilder selectSb = new StringBuilder();
        String selectString = "select ";
        String inString = "IN (";
        for (int tableIndex = 0; tableIndex < tableNames.length; tableIndex++) {
            if (updates[tableIndex] != null) {
                // We introduce a dummy update statement that we react upon
                if (subclassTableNames.contains(tableNames[tableIndex])) {
                    secondaryTableUpdates[tableIndex] = null;
                } else {
                    sb.setLength(0);
                    selectSb.setLength(0);

                    boolean affected = false;
                    String idSubselect = updates[tableIndex].substring(updates[tableIndex].lastIndexOf(inString) + inString.length(), updates[tableIndex].length() - 1);

                    sb.append("insert into ").append(tableNames[tableIndex]);
                    String[] keyColumnNames = columnNames[tableIndex];
                    sb.append('(');

                    final List<AssignmentSpecification> assignmentSpecifications = walker.getAssignmentSpecifications();
                    for (AssignmentSpecification assignmentSpecification : assignmentSpecifications) {
                        if (assignmentSpecification.affectsTable(tableNames[tableIndex])) {
                            String sqlAssignmentFragment = assignmentSpecification.getSqlAssignmentFragment();
                            int eqIndex = sqlAssignmentFragment.indexOf('=');
                            sb.append(sqlAssignmentFragment, 0, eqIndex);
                            sb.append(',');
                            selectSb.append(sqlAssignmentFragment, eqIndex + 1, sqlAssignmentFragment.length());
                            selectSb.append(',');

                            affected = true;
                        }
                    }
                    if (affected) {
                        for (int i = 0; i < keyColumnNames.length; i++) {
                            sb.append(keyColumnNames[i]);
                            sb.append(',');
                        }
                        sb.setCharAt(sb.length() - 1, ')');
                        sb.append(' ').append(selectString);
                        sb.append(selectSb);
                        sb.append(idSubselect, selectString.length(), idSubselect.length());
                        sb.append(" where not exists (select 1 from ");
                        sb.append(tableNames[tableIndex]);
                        sb.append(" a where ");

                        for (int i = 0; i < keyColumnNames.length; i++) {
                            sb.append("a.");
                            sb.append(keyColumnNames[i]);
                            sb.append(" = ");
                            sb.append(keyColumnNames[i]);
                            sb.append(" and ");
                        }

                        sb.setLength(sb.length() - " and ".length());
                        sb.append(")");

                        secondaryTableInserts[tableIndex] = sb.toString();
                    }

                    updates[tableIndex] = "";
                }
            }
        }

        this.secondaryTableUpdates = secondaryTableUpdates;
        this.secondaryTableInserts = secondaryTableInserts;
    }

    @Override
    public int execute(SharedSessionContractImplementor s, QueryParameters queryParameters) {
        final SessionImplementor session = (SessionImplementor) s;
        final JdbcCoordinator jdbcCoordinator = session.getJdbcCoordinator();

        Object jdbcCoordinatorProxy = Proxy.newProxyInstance(jdbcCoordinator.getClass().getClassLoader(), new Class[]{JdbcCoordinator.class}, new JdbcCoordinatorInvocationHandler(jdbcCoordinator, new DelegatingStatementPreparerImpl(jdbcCoordinator.getStatementPreparer()) {
            PreparedStatement statementProxy;
            SecondaryTableUpdateSupportingPreparedStatementInvocationHandler invocationHandler;

            @Override
            public PreparedStatement prepareStatement(String sql, boolean isCallable) {
                if (sql.isEmpty()) {
                    // Return the statement proxy which collects parameters and then executes update/insert statements for secondary tables
                    invocationHandler.prepareNext();
                    return statementProxy;
                } else {
                    PreparedStatement insertStatement = super.prepareStatement(sql, isCallable);
                    this.invocationHandler = new SecondaryTableUpdateSupportingPreparedStatementInvocationHandler(session, jdbcCoordinator.getStatementPreparer(), insertStatement, secondaryTableUpdates, secondaryTableInserts);
                    this.statementProxy = (PreparedStatement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{PreparedStatement.class}, invocationHandler);
                    return statementProxy;
                }
            }
        }));
        SessionImplementor sessionProxy = (SessionImplementor) Proxy.newProxyInstance(session.getClass().getClassLoader(), new Class[]{SessionImplementor.class, EventSource.class}, new Hibernate53SessionInvocationHandler(session, jdbcCoordinatorProxy));

        return delegate.execute(sessionProxy, queryParameters);
    }

    @Override
    public Queryable getTargetedQueryable() {
        return delegate.getTargetedQueryable();
    }

    @Override
    public String[] getSqlStatements() {
        return delegate.getSqlStatements();
    }
}
