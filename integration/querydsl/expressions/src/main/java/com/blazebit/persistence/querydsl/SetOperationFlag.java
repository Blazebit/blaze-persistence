/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.querydsl;

import com.querydsl.core.QueryFlag;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.Expression;

/**
 * A special type of {@code QueryFlag} that indicates that a subquery represents a set operation.
 *
 * @apiNote In {@link com.querydsl.sql.ProjectableSQLQuery}, the union operation, if any, is stored in
 * a field inside the query. If the query is a union operator, the subquery node represents itself as
 * the set operation operation node opon visiting. However, due to this way, a visitor can't access the
 * {@link QueryMetadata} of the {@link com.querydsl.core.types.SubQueryExpression} of the set operation,
 * which is necessairy for determining the {@link com.querydsl.core.QueryModifiers} and order specifiers
 * in subqueries. As a result, we decided to take a different approach and model set operation in a
 * query flag instead. This allows visitors to visit the subquery as-is, and decide how they want to
 * handle the set operation within the query flag.
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
public class SetOperationFlag extends QueryFlag {

    private static final long serialVersionUID = -1817935672631547851L;

    public SetOperationFlag(Expression<?> flag) {
        super(Position.START_OVERRIDE, flag);
    }

    public static SetOperationFlag getSetOperationFlag(QueryMetadata queryMetadata) {
        for (QueryFlag flag : queryMetadata.getFlags()) {
            if (flag instanceof SetOperationFlag) {
                return (SetOperationFlag) flag;
            }
        }
        return null;
    }

}
