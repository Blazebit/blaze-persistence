/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.persistence.integration.hibernate.base.HibernateAccess;
import com.blazebit.persistence.integration.hibernate.base.HibernateReturningResult;
import com.blazebit.persistence.spi.DbmsDialect;

import org.hibernate.ScrollMode;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.query.spi.DomainQueryExecutionContext;
import org.hibernate.query.spi.ScrollableResultsImplementor;
import org.hibernate.query.sqm.internal.SqmJdbcExecutionContextAdapter;
import org.hibernate.sql.ast.spi.SqlSelection;
import org.hibernate.sql.ast.tree.expression.JdbcParameter;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcOperationQuery;
import org.hibernate.sql.exec.spi.JdbcOperationQueryDelete;
import org.hibernate.sql.exec.spi.JdbcOperationQueryMutation;
import org.hibernate.sql.exec.spi.JdbcOperationQuerySelect;
import org.hibernate.sql.exec.spi.JdbcOperationQueryUpdate;
import org.hibernate.sql.exec.spi.JdbcParameterBinder;
import org.hibernate.sql.exec.spi.JdbcParameterBindings;
import org.hibernate.sql.results.jdbc.spi.JdbcValuesMapping;
import org.hibernate.sql.results.spi.ListResultsConsumer;
import org.hibernate.sql.results.spi.RowTransformer;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.6.7
 */
public class Hibernate7Access implements HibernateAccess {

    @Override
    public ExecutionContext wrapExecutionContext(ExecutionContext executionContext, DbmsDialect dbmsDialect, String[][] returningColumns, int[] returningColumnTypes, HibernateReturningResult<Object[]> returningResult) {
        SharedSessionContractImplementor session = executionContext.getSession();
        JdbcCoordinator jdbcCoordinator = session.getJdbcCoordinator();

        Object jdbcCoordinatorProxy = Proxy.newProxyInstance(jdbcCoordinator.getClass().getClassLoader(), new Class[]{JdbcCoordinator.class}, new JdbcCoordinatorInvocationHandler(jdbcCoordinator, session.getFactory(), dbmsDialect, returningColumnTypes, returningResult));
        Object sessionProxy = Proxy.newProxyInstance(session.getClass().getClassLoader(), new Class[]{SessionImplementor.class, EventSource.class}, new Hibernate7SessionInvocationHandler( session, jdbcCoordinatorProxy));
        Object exceutionContextProxy = Proxy.newProxyInstance(session.getClass().getClassLoader(), new Class[]{ ExecutionContext.class }, new Hibernate7ExecutionContextInvocationHandler( executionContext, sessionProxy));
        return (ExecutionContext) exceutionContextProxy;
    }

    @Override
    public JdbcOperationQueryMutation createJdbcDelete(JdbcOperationQueryMutation delete, StringBuilder newSb) {
        return new JdbcOperationQueryDelete(
                newSb.toString(),
                delete.getParameterBinders(),
                delete.getAffectedTableNames(),
                delete.getAppliedParameters()
        );
    }

    @Override
    public JdbcOperationQueryMutation createJdbcUpdate(
            String sql,
            List<JdbcParameterBinder> parameterBinders,
            Set<String> affectedTableNames) {
        return new JdbcOperationQueryUpdate(
                sql,
                parameterBinders,
                affectedTableNames,
                Collections.emptyMap()
        );
    }

    @Override
    public JdbcOperationQuery createJdbcSelect(
            String sql,
            List<JdbcParameterBinder> parameterBinders,
            JdbcOperationQuery original,
            Set<String> affectedTableNames) {
        JdbcOperationQuerySelect jdbcSelect = (JdbcOperationQuerySelect) original;
        return new JdbcOperationQuerySelect(
                sql,
                parameterBinders,
                jdbcSelect.getJdbcValuesMappingProducer(),
                affectedTableNames
//                ,jdbcSelect.getRowsToSkip(),
//                jdbcSelect.getMaxRows(),
//                jdbcSelect.getAppliedParameters(),
//                jdbcSelect.getLockStrategy(),
//                jdbcSelect.getOffsetParameter(),
//                jdbcSelect.getLimitParameter()
        );
    }

    @Override
    public JdbcOperationQuery createFullJdbcSelect(
            String sql,
            List<JdbcParameterBinder> parameterBinders,
            JdbcOperationQuery original,
            Set<String> affectedTableNames) {
        JdbcOperationQuerySelect jdbcSelect = (JdbcOperationQuerySelect) original;
        return new JdbcOperationQuerySelect(
                sql,
                parameterBinders,
                jdbcSelect.getJdbcValuesMappingProducer(),
                affectedTableNames,
                jdbcSelect.getRowsToSkip(),
                jdbcSelect.getMaxRows(),
                jdbcSelect.getAppliedParameters(),
                jdbcSelect.getLockStrategy(),
                jdbcSelect.getOffsetParameter(),
                jdbcSelect.getLimitParameter()
        );
    }

    @Override
    public JdbcParameter getLimitParameter(JdbcOperationQuery query) {
        return ((JdbcOperationQuerySelect) query).getLimitParameter();
    }

    @Override
    public JdbcParameter getOffsetParameter(JdbcOperationQuery query) {
        return ((JdbcOperationQuerySelect) query).getOffsetParameter();
    }

    @Override
    public ExecutionContext createExecutionContextAdapter(DomainQueryExecutionContext executionContext, JdbcOperationQuery query) {
        return new SqmJdbcExecutionContextAdapter(executionContext, (JdbcOperationQuerySelect) query);
    }

    @Override
    public <R> List<R> list(
            JdbcOperationQuery jdbcSelect,
            JdbcParameterBindings jdbcParameterBindings,
            ExecutionContext executionContext,
            RowTransformer<R> rowTransformer,
            ListResultsConsumer.UniqueSemantic uniqueSemantic) {
        return executionContext.getSession().getFactory().getJdbcServices().getJdbcSelectExecutor().list(
                (JdbcOperationQuerySelect) jdbcSelect,
                jdbcParameterBindings,
                executionContext,
                rowTransformer,
                uniqueSemantic
        );
    }

    @Override
    public <R> ScrollableResultsImplementor<R> scroll(
            JdbcOperationQuery jdbcSelect,
            ScrollMode scrollMode,
            JdbcParameterBindings jdbcParameterBindings,
            ExecutionContext executionContext,
            RowTransformer<R> rowTransformer) {
        return executionContext.getSession().getFactory().getJdbcServices().getJdbcSelectExecutor().scroll(
                (JdbcOperationQuerySelect) jdbcSelect,
                scrollMode,
                jdbcParameterBindings,
                executionContext,
                rowTransformer
        );
    }

    @Override
    public int[] getReturningColumnTypes(JdbcOperationQuery queryPlan, SessionFactoryImplementor sfi) {
        JdbcValuesMapping jdbcValuesMapping = ((JdbcOperationQuerySelect) queryPlan).getJdbcValuesMappingProducer().resolve( null, null, sfi);
        List<SqlSelection> sqlSelections = jdbcValuesMapping.getSqlSelections();
        List<Integer> sqlTypes = new ArrayList<>(sqlSelections.size());

        for (int i = 0; i < sqlSelections.size(); i++) {
            sqlTypes.add(sqlSelections.get(i).getExpressionType().getSingleJdbcMapping().getJdbcType().getDefaultSqlTypeCode());
        }

        int[] returningColumnTypes = new int[sqlTypes.size()];
        for (int i = 0; i < sqlTypes.size(); i++) {
            returningColumnTypes[i] = sqlTypes.get(i);
        }

        return returningColumnTypes;
    }
}
