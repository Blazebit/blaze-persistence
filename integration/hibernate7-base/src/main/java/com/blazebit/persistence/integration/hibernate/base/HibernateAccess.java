/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate.base;

import java.util.List;
import java.util.Set;

import com.blazebit.persistence.spi.DbmsDialect;

import org.hibernate.ScrollMode;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.spi.DomainQueryExecutionContext;
import org.hibernate.query.spi.ScrollableResultsImplementor;
import org.hibernate.sql.ast.tree.expression.JdbcParameter;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcOperationQuery;
import org.hibernate.sql.exec.spi.JdbcOperationQueryMutation;
import org.hibernate.sql.exec.spi.JdbcParameterBinder;
import org.hibernate.sql.exec.spi.JdbcParameterBindings;
import org.hibernate.sql.results.spi.ListResultsConsumer;
import org.hibernate.sql.results.spi.RowTransformer;

/**
 * @author Christian Beikov
 * @since 1.6.7
 */
public interface HibernateAccess {

    public ExecutionContext wrapExecutionContext(ExecutionContext executionContext, DbmsDialect dbmsDialect, String[][] returningColumns, int[] returningColumnTypes, HibernateReturningResult<Object[]> returningResult);
    public JdbcOperationQueryMutation createJdbcDelete(JdbcOperationQueryMutation delete, StringBuilder newSb);
    public JdbcOperationQueryMutation createJdbcUpdate(String sql, List<JdbcParameterBinder> parameterBinders, Set<String> affectedTableNames);
    public JdbcOperationQuery createJdbcSelect(String sql, List<JdbcParameterBinder> parameterBinders, JdbcOperationQuery original, Set<String> affectedTableNames);
    public JdbcOperationQuery createFullJdbcSelect(String sql, List<JdbcParameterBinder> parameterBinders, JdbcOperationQuery original, Set<String> affectedTableNames);
    public JdbcParameter getLimitParameter(JdbcOperationQuery query);
    public JdbcParameter getOffsetParameter(JdbcOperationQuery query);
    public ExecutionContext createExecutionContextAdapter(DomainQueryExecutionContext executionContext, JdbcOperationQuery query);
    public int[] getReturningColumnTypes(JdbcOperationQuery queryPlan, SessionFactoryImplementor sfi);
    public <R> List<R> list(JdbcOperationQuery jdbcSelect, JdbcParameterBindings jdbcParameterBindings, ExecutionContext executionContext, RowTransformer<R> rowTransformer, ListResultsConsumer.UniqueSemantic uniqueSemantic);
    public <R> ScrollableResultsImplementor<R> scroll(JdbcOperationQuery jdbcSelect, ScrollMode scrollMode, JdbcParameterBindings jdbcParameterBindings, ExecutionContext executionContext, RowTransformer<R> rowTransformer);
}
