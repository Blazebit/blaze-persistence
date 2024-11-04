/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * {@code WindowFunction} is a builder for window function expressions.
 * Analog to {@link com.querydsl.sql.WindowFunction}.
 *
 * @param <A> expression type
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
@SuppressWarnings("unused")
public class WindowFunction<A> extends WindowDefinition<WindowFunction<A>, A> {

    private static final long serialVersionUID = 5108488516938771478L;

    private final Expression<A> target;

    @Nullable
    private transient volatile SimpleExpression<A> value;

    /**
     * Create a new {@code WindowFunction} that wraps an {@code Expression}.
     *
     * @param expr the expression
     */
    public WindowFunction(Expression<A> expr) {
        super(expr.getType());
        this.target = expr;
    }


    /**
     * Create a new {@code WindowFunction} that wraps an {@code Expression}.
     *
     * @param expr the expression
     * @param baseWindowName the base window name
     */
    public WindowFunction(Expression<A> expr, String baseWindowName) {
        super(expr.getType(), baseWindowName);
        this.target = expr;
    }

    @Override
    public SimpleExpression<A> getValue() {
        SimpleExpression<A> value = this.value;
        if (value == null) {
            this.value = value = Expressions.template(target.getType(), "{0} over ({1})", target, super.getValue());
        }
        return value;
    }

    /**
     * Create an alias for the expression.
     *
     * @param alias The alias
     * @return alias expression
     */
    public SimpleExpression<A> as(Expression<A> alias) {
        return Expressions.operation(getType(), Ops.ALIAS, this, alias);
    }

    /**
     * Create an alias for the expression.
     *
     * @param alias The alias
     * @return alias expression
     */
    public SimpleExpression<A> as(String alias) {
        return Expressions.operation(getType(), Ops.ALIAS, this, ExpressionUtils.path(getType(), alias));
    }

    /**
     * Create a {@code this == right} expression
     *
     * @param expr rhs of the comparison
     * @return this == right
     */
    public BooleanExpression eq(Expression<A> expr) {
        return getValue().eq(expr);
    }

    /**
     * Create a {@code this == right} expression
     *
     * @param arg rhs of the comparison
     * @return this == right
     */
    public BooleanExpression eq(A arg) {
        return getValue().eq(arg);
    }

    /**
     * Create a {@code this <> right} expression
     *
     * @param expr rhs of the comparison
     * @return this != right
     */
    public BooleanExpression ne(Expression<A> expr) {
        return getValue().ne(expr);
    }

    /**
     * Create a {@code this <> right} expression
     *
     * @param arg rhs of the comparison
     * @return this != right
     */
    public BooleanExpression ne(A arg) {
        return getValue().ne(arg);
    }

    @Override
    @SuppressWarnings("EqualsHashCode") // Hashcode is declared final in MutableExpressionBase, the implementation there is safe with this equals implementation
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else if (!super.equals(o)) {
            return false;
        }
        WindowFunction<?> that = (WindowFunction<?>) o;
        return Objects.equals(target, that.target) && super.equals(o);
    }

}
