/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Operation;
import com.querydsl.core.types.SubQueryExpression;

/**
 * Utility methods for generating set operations.
 *
 * Analog to {@code com.querydsl.sql.UnionUtils}.
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
public final class SetUtils {

    protected SetUtils() {
    }

    /**
     * Create a set operation
     *
     * @param setOperation Set operation to use
     * @param wrapSets Whether or not to wrap set left nested operation expressions.
     *    In most cases, expressions are assumed in CNF with explicit precedence.
     *    In some cases however, you want the operator to over precedence, i.e.
     *    in a chain of operations that are left folded
     * @param expressions Operands for the set operation
     * @param <T> Set operation result type
     * @return The set operation
     */
    @SafeVarargs
    public static <T> Expression<T> setOperation(JPQLNextOps setOperation, boolean wrapSets, Expression<T>... expressions) {
        Expression<T> result = expressions[0];
        SetOperationVisitor<T> setOperationVisitor = new SetOperationVisitor<>(setOperation, wrapSets);
        for (int i = 1; i < expressions.length; i++) {
            Expression<T> expression = expressions[i];
            if (Boolean.TRUE.equals(expression.accept(NotEmptySetVisitor.INSTANCE, null))) {
                result = result.accept(setOperationVisitor, expression);
            }
        }
        return result;
    }

    private static JPQLNextOps getLeftNestedSetOperation(JPQLNextOps setOperation) {
        switch (setOperation) {
            case SET_UNION:
                return JPQLNextOps.LEFT_NESTED_SET_UNION;
            case SET_UNION_ALL:
                return JPQLNextOps.LEFT_NESTED_SET_UNION_ALL;
            case SET_INTERSECT:
                return JPQLNextOps.LEFT_NESTED_SET_INTERSECT;
            case SET_INTERSECT_ALL:
                return JPQLNextOps.LEFT_NESTED_SET_INTERSECT_ALL;
            case SET_EXCEPT:
                return JPQLNextOps.LEFT_NESTED_SET_EXCEPT;
            case SET_EXCEPT_ALL:
                return JPQLNextOps.LEFT_NESTED_SET_EXCEPT_ALL;
            default:
                return setOperation;
        }
    }

    /**
     * Create a union set operation
     *
     * @param expressions Operands for the set operation
     * @param <T> Set operation result type
     * @return The set operation
     */
    @SafeVarargs
    public static <T> Expression<T> union(Expression<T>... expressions) {
        return setOperation(JPQLNextOps.SET_UNION, true, expressions);
    }

    /**
     * Create a union all set operation
     *
     * @param expressions Operands for the set operation
     * @param <T> Set operation result type
     * @return The set operation
     */
    @SafeVarargs
    public static <T> Expression<T> unionAll(Expression<T>... expressions) {
        return setOperation(JPQLNextOps.SET_UNION_ALL, true, expressions);
    }

    /**
     * Create a intersect set operation
     *
     * @param expressions Operands for the set operation
     * @param <T> Set operation result type
     * @return The set operation
     */
    @SafeVarargs
    public static <T> Expression<T> intersect(Expression<T>... expressions) {
        return setOperation(JPQLNextOps.SET_INTERSECT, true, expressions);
    }

    /**
     * Create a intersect all set operation
     *
     * @param expressions Operands for the set operation
     * @param <T> Set operation result type
     * @return The set operation
     */
    @SafeVarargs
    public static <T> Expression<T> intersectAll(Expression<T>... expressions) {
        return setOperation(JPQLNextOps.SET_INTERSECT_ALL, true, expressions);
    }

    /**
     * Create a except set operation
     *
     * @param expressions Operands for the set operation
     * @param <T> Set operation result type
     * @return The set operation
     */
    @SafeVarargs
    public static <T> Expression<T> except(Expression<T>... expressions) {
        return setOperation(JPQLNextOps.SET_EXCEPT, true, expressions);
    }

    /**
     * Create a except all set operation
     *
     * @param expressions Operands for the set operation
     * @param <T> Set operation result type
     * @return The set operation
     */
    @SafeVarargs
    public static <T> Expression<T> exceptAll(Expression<T>... expressions) {
        return setOperation(JPQLNextOps.SET_EXCEPT_ALL, true, expressions);
    }

    /**
     * Visitor that creates a set operation between the lhs and rhs.
     * If the lhs is a set operation itself and wrap sets is enabled, the left nested equivalent set operation is used instead.
     * Wrap sets is for example useful when expressions are expected to be in CNF.
     *
     * @param <T> Set operation result type
     */
    private static class SetOperationVisitor<T> extends DefaultVisitorImpl<Expression<T>, Expression<T>> {

        private final boolean wrapSets;
        private final JPQLNextOps setOperation;
        private final JPQLNextOps leftNestedOperation;

        public SetOperationVisitor(JPQLNextOps setOperation, boolean wrapSets) {
            this.wrapSets = wrapSets;
            this.leftNestedOperation = getLeftNestedSetOperation(setOperation);
            this.setOperation = setOperation;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Expression<T> visit(Operation<?> lhs, Expression<T> rhs) {
            return (Expression<T>) ExpressionUtils.operation(lhs.getType(), wrapSets ? leftNestedOperation : setOperation, lhs, rhs);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Expression<T> visit(SubQueryExpression<?> lhs, Expression<T> rhs) {
            SetOperationFlag setOperationFlag = SetOperationFlag.getSetOperationFlag(lhs.getMetadata());
            boolean nestedSet = setOperationFlag != null;
            return (Expression<T>) ExpressionUtils.operation(lhs.getType(), nestedSet && wrapSets ? leftNestedOperation : setOperation, lhs, rhs);
        }

    }
}
