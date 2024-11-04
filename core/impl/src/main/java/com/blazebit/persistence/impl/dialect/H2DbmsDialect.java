/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.dialect;

import java.util.Map;

import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.DeleteJoinStyle;
import com.blazebit.persistence.spi.LateralStyle;
import com.blazebit.persistence.spi.UpdateJoinStyle;
import com.blazebit.persistence.spi.ValuesStrategy;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class H2DbmsDialect extends DefaultDbmsDialect {

    public H2DbmsDialect() {
    }

    public H2DbmsDialect(Map<Class<?>, String> childSqlTypes) {
        super(childSqlTypes);
    }

    @Override
    public boolean supportsAnsiRowValueConstructor() {
        // At least Hibernate thinks so. We need this to get embeddable splitting working
        return false;
    }

    @Override
    public boolean supportsCountTuple() {
        return true;
    }

    @Override
    public boolean supportsReturningAllGeneratedKeys() {
        return false;
    }
    
    @Override
    public boolean supportsWithClause() {
        return true;
    }

    @Override
    public boolean supportsNonRecursiveWithClause() {
        return false;
    }

    @Override
    public String getWithClause(boolean recursive) {
        return "with recursive";
    }

    @Override
    public Map<String, String> appendExtendedSql(StringBuilder sqlSb, DbmsStatementType statementType, boolean isSubquery, boolean isEmbedded, StringBuilder withClause, String limit, String offset, String dmlAffectedTable, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates) {
        boolean addParenthesis = isSubquery && sqlSb.length() > 0 && sqlSb.charAt(0) != '(';
        if (addParenthesis) {
            sqlSb.insert(0, '(');
        }
        
        if (isSubquery && returningColumns != null) {
            throw new IllegalArgumentException("Returning columns in a subquery is not possible for this dbms!");
        }
        
        // NOTE: this only works for insert and select statements, but H2 does not support CTEs in modification queries anyway so it's ok
        if (withClause != null) {
            sqlSb.insert(SqlUtils.SELECT_FINDER.indexIn(sqlSb, 0, sqlSb.length()), withClause);
        }
        if (limit != null) {
            appendLimit(sqlSb, isSubquery, limit, offset);
        }
        
        if (addParenthesis) {
            sqlSb.append(')');
        }
        
        return null;
    }
    
    @Override
    public boolean supportsWithClauseInModificationQuery() {
        // As of 1.4.199 this is supported
        return true;
    }

    @Override
    public ValuesStrategy getValuesStrategy() {
        return ValuesStrategy.SELECT_VALUES;
    }

    @Override
    public boolean needsUniqueSelectItemNamesAlsoWhenTableColumnAliasing() {
        return true;
    }

    @Override
    public boolean supportsBooleanAggregation() {
        return true;
    }

    @Override
    public boolean supportsWindowNullPrecedence() {
        return true;
    }

    @Override
    public boolean isNullSmallest() {
        // Actually, H2 always shows NULL first, regardless of the ordering, but we don't care because it supports null precedence handling
        return true;
    }

    @Override
    public LateralStyle getLateralStyle() {
        return LateralStyle.NONE;
    }

    @Override
    public DeleteJoinStyle getDeleteJoinStyle() {
        return DeleteJoinStyle.MERGE;
    }

    @Override
    public UpdateJoinStyle getUpdateJoinStyle() {
        return UpdateJoinStyle.MERGE;
    }

    @Override
    public Character getDefaultEscapeCharacter() {
        // H2 is non SQL-standard compliant in this regard
        return '\\';
    }

    @Override
    public boolean supportsArbitraryLengthMultiset() {
        return true;
    }
}
