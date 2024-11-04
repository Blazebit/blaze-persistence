/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.querydsl;

import com.querydsl.core.types.Operation;
import com.querydsl.core.types.SubQueryExpression;

import static com.blazebit.persistence.querydsl.SetOperationFlag.getSetOperationFlag;

/**
 * Visitor implementation that checks if a query is empty (i.e. has no default joins).
 * Empty queries are removed from set operations, as they cannot be represented in SQL.
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
public class NotEmptySetVisitor extends DefaultVisitorImpl<Boolean, Void> {

    public static final NotEmptySetVisitor INSTANCE = new NotEmptySetVisitor();

    @Override
    public Boolean visit(Operation<?> operation, Void aVoid) {
        return operation.getArg(0).accept(this, aVoid);
    }

    @Override
    public Boolean visit(SubQueryExpression<?> subQueryExpression, Void aVoid) {
        SetOperationFlag setOperationFlag = getSetOperationFlag(subQueryExpression.getMetadata());
        return setOperationFlag != null && setOperationFlag.getFlag().accept(this, aVoid)
                || !subQueryExpression.getMetadata().getJoins().isEmpty();
    }
}
