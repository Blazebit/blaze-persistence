/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.dialect;

import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.DeleteJoinStyle;
import com.blazebit.persistence.spi.LateralStyle;
import com.blazebit.persistence.spi.UpdateJoinStyle;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class MySQL8DbmsDialect extends MySQLDbmsDialect {

    public MySQL8DbmsDialect() {
        super();
    }

    public MySQL8DbmsDialect(Map<Class<?>, String> childSqlTypes) {
        super(childSqlTypes);
    }

    @Override
    public boolean supportsWindowFunctions() {
        return true;
    }

    @Override
    public boolean supportsWithClause() {
        return true;
    }

    @Override
    public boolean supportsNonRecursiveWithClause() {
        return true;
    }

    @Override
    public LateralStyle getLateralStyle() {
        return LateralStyle.LATERAL;
    }

    @Override
    public DeleteJoinStyle getDeleteJoinStyle() {
        return DeleteJoinStyle.FROM;
    }

    @Override
    public UpdateJoinStyle getUpdateJoinStyle() {
        return UpdateJoinStyle.REFERENCE;
    }

    @Override
    public String getWithClause(boolean recursive) {
        if (recursive) {
            return "with recursive";
        } else {
            return "with";
        }
    }

    @Override
    public Map<String, String> appendExtendedSql(StringBuilder sqlSb, DbmsStatementType statementType, boolean isSubquery, boolean isEmbedded, StringBuilder withClause, String limit, String offset, String dmlAffectedTable, String[] returningColumns, Map<DbmsModificationState, String> includedModificationStates) {
        boolean addParenthesis = isSubquery && sqlSb.length() > 0 && sqlSb.charAt(0) != '(';
        if (addParenthesis) {
            sqlSb.insert(0, '(');
        }

        if (withClause != null) {
            if (statementType == DbmsStatementType.INSERT) {
                sqlSb.insert(SqlUtils.SELECT_FINDER.indexIn(sqlSb, 0, sqlSb.length()), withClause);
            } else {
                sqlSb.insert(0, withClause);
            }
        }
        if (limit != null) {
            appendLimit(sqlSb, isSubquery, limit, offset);
        }
        if (isSubquery && !supportsModificationQueryInWithClause() && returningColumns != null) {
            throw new IllegalArgumentException("Returning columns in a subquery is not possible for this dbms!");
        }

        if (addParenthesis) {
            sqlSb.append(')');
        }

        return null;
    }
}
