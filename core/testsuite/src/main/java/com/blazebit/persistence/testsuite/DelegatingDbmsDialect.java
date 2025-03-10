/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.DbmsLimitHandler;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.DeleteJoinStyle;
import com.blazebit.persistence.spi.LateralStyle;
import com.blazebit.persistence.spi.OrderByElement;
import com.blazebit.persistence.spi.SetOperationType;
import com.blazebit.persistence.spi.UpdateJoinStyle;
import com.blazebit.persistence.spi.ValuesStrategy;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.2.0
 */
public class DelegatingDbmsDialect implements DbmsDialect {

    private final DbmsDialect delegate;

    public DelegatingDbmsDialect(DbmsDialect delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean supportsWindowFunctions() {
        return delegate.supportsWindowFunctions();
    }

    @Override
    public boolean supportsWithClause() {
        return delegate.supportsWithClause();
    }

    @Override
    public boolean supportsNonRecursiveWithClause() {
        return delegate.supportsNonRecursiveWithClause();
    }

    @Override
    public boolean supportsWithClauseHead() {
        return delegate.supportsWithClauseHead();
    }

    @Override
    public String getWithClause(boolean recursive) {
        return delegate.getWithClause(recursive);
    }

    @Override
    public Map<String, String> appendExtendedSql(StringBuilder sqlSb, DbmsStatementType statementType, boolean isSubquery, boolean isEmbedded, StringBuilder withClause, String limit, String offset, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates) {
        return delegate.appendExtendedSql(sqlSb, statementType, isSubquery, isEmbedded, withClause, limit, offset, returningColumns, includedModificationStates);
    }

    @Override
    public Map<String, String> appendExtendedSql(StringBuilder sqlSb, DbmsStatementType statementType, boolean isSubquery, boolean isEmbedded, StringBuilder withClause, String limit, String offset, String dmlAffectedTable, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates) {
        return delegate.appendExtendedSql(sqlSb, statementType, isSubquery, isEmbedded, withClause, limit, offset, dmlAffectedTable, returningColumns, includedModificationStates);
    }

    @Override
    public void appendSet(StringBuilder sqlSb, SetOperationType setType, boolean isSubquery, List<String> operands, List<? extends OrderByElement> orderByElements, String limit, String offset) {
        delegate.appendSet(sqlSb, setType, isSubquery, operands, orderByElements, limit, offset);
    }

    @Override
    public void appendOrderByElement(StringBuilder sqlSb, OrderByElement orderByElement, String[] aliases) {
        delegate.appendOrderByElement(sqlSb, orderByElement, aliases);
    }

    @Override
    public DbmsLimitHandler createLimitHandler() {
        return delegate.createLimitHandler();
    }

    @Override
    public boolean supportsLimitWithoutOrderBy() {
        return delegate.supportsLimitWithoutOrderBy();
    }

    @Override
    public boolean supportsLimitInQuantifiedPredicateSubquery() {
        return delegate.supportsLimitInQuantifiedPredicateSubquery();
    }

    @Override
    public boolean supportsNestedCorrelations() {
        return delegate.supportsNestedCorrelations();
    }

    @Override
    public boolean supportsWithClauseInModificationQuery() {
        return delegate.supportsWithClauseInModificationQuery();
    }

    @Override
    public boolean supportsModificationQueryInWithClause() {
        return delegate.supportsModificationQueryInWithClause();
    }

    @Override
    public boolean usesExecuteUpdateWhenWithClauseInModificationQuery() {
        return delegate.usesExecuteUpdateWhenWithClauseInModificationQuery();
    }

    @Override
    public boolean supportsReturningGeneratedKeys() {
        return delegate.supportsReturningGeneratedKeys();
    }

    @Override
    public boolean supportsReturningAllGeneratedKeys() {
        return delegate.supportsReturningAllGeneratedKeys();
    }

    @Override
    public boolean supportsReturningColumns() {
        return delegate.supportsReturningColumns();
    }

    @Override
    public boolean supportsGroupByExpressionInHavingMatching() {
        return delegate.supportsGroupByExpressionInHavingMatching();
    }

    @Override
    public boolean supportsComplexJoinOn() {
        return delegate.supportsComplexJoinOn();
    }

    @Override
    public boolean supportsUnion(boolean all) {
        return delegate.supportsUnion(all);
    }

    @Override
    public boolean supportsIntersect(boolean all) {
        return delegate.supportsIntersect(all);
    }

    @Override
    public boolean supportsExcept(boolean all) {
        return delegate.supportsExcept(all);
    }

    @Override
    public boolean supportsJoinsInRecursiveCte() {
        return delegate.supportsJoinsInRecursiveCte();
    }

    @Override
    public boolean supportsAnsiRowValueConstructor() {
        return delegate.supportsAnsiRowValueConstructor();
    }

    @Override
    public boolean supportsRowValueConstructor() {
        return delegate.supportsRowValueConstructor();
    }

    @Override
    public boolean supportsFilterClause() {
        return delegate.supportsFilterClause();
    }

    @Override
    public boolean supportsNullPrecedence() {
        return delegate.supportsNullPrecedence();
    }

    @Override
    public boolean supportsWindowNullPrecedence() {
        return delegate.supportsWindowNullPrecedence();
    }

    @Override
    public boolean supportsBooleanAggregation() {
        return delegate.supportsBooleanAggregation();
    }

    @Override
    public boolean isNullSmallest() {
        return delegate.isNullSmallest();
    }

    @Override
    public boolean requiresNullCast() {
        return delegate.requiresNullCast();
    }

    @Override
    public LateralStyle getLateralStyle() {
        return delegate.getLateralStyle();
    }

    @Override
    public String getPhysicalRowId() {
        return delegate.getPhysicalRowId();
    }

    @Override
    public DeleteJoinStyle getDeleteJoinStyle() {
        return delegate.getDeleteJoinStyle();
    }

    @Override
    public UpdateJoinStyle getUpdateJoinStyle() {
        return delegate.getUpdateJoinStyle();
    }

    @Override
    public boolean supportsArbitraryLengthMultiset() {
        return delegate.supportsArbitraryLengthMultiset();
    }

    @Override
    public boolean supportsFullRowValueComparison() {
        return delegate.supportsFullRowValueComparison();
    }

    @Override
    public boolean supportsCountTuple() {
        return delegate.supportsCountTuple();
    }

    @Override
    public String getSqlType(Class<?> castType) {
        return delegate.getSqlType(castType);
    }

    @Override
    public ValuesStrategy getValuesStrategy() {
        return delegate.getValuesStrategy();
    }

    @Override
    public boolean needsUniqueSelectItemNamesAlsoWhenTableColumnAliasing() {
        return delegate.needsUniqueSelectItemNamesAlsoWhenTableColumnAliasing();
    }

    @Override
    public boolean needsCastParameters() {
        return delegate.needsCastParameters();
    }

    @Override
    public String getDummyTable() {
        return delegate.getDummyTable();
    }

    @Override
    public String cast(String expression, String sqlType) {
        return delegate.cast(expression, sqlType);
    }

    @Override
    public Character getDefaultEscapeCharacter() {
        return delegate.getDefaultEscapeCharacter();
    }

    @Override
    public boolean needsReturningSqlTypes() {
        return delegate.needsReturningSqlTypes();
    }

    @Override
    public int getPrepareFlags() {
        return delegate.getPrepareFlags();
    }

    @Override
    public PreparedStatement prepare(PreparedStatement ps, int[] returningSqlTypes) throws SQLException {
        return delegate.prepare(ps, returningSqlTypes);
    }

    @Override
    public ResultSet extractReturningResult(PreparedStatement ps) throws SQLException {
        return delegate.extractReturningResult(ps);
    }
}
