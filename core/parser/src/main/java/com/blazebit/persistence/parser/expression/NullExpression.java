/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

/**
 *
 * @author Christian Beikov
 * @since 1.0.1
 */
public class NullExpression extends AbstractExpression implements LiteralExpression<Object> {

    public static final NullExpression INSTANCE = new NullExpression();

    @Override
    public NullExpression copy(ExpressionCopyContext copyContext) {
        return this;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "NULL";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NullExpression;
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public Object getValue() {
        return null;
    }
}
