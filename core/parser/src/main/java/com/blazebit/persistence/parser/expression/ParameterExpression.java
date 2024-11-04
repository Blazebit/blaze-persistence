/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class ParameterExpression extends AbstractExpression {

    private String name;
    private final Object value;
    private boolean collectionValued;

    public ParameterExpression(String name) {
        this(name, null);
    }

    public ParameterExpression(String name, Object value) {
        this(name, value, false);
    }

    public ParameterExpression(String name, Object value, boolean collectionValued) {
        this.name = name;
        this.value = value;
        this.collectionValued = collectionValued;
    }

    @Override
    public ParameterExpression copy(ExpressionCopyContext copyContext) {
        return new ParameterExpression(copyContext.getNewParameterName(name), value, collectionValued);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public boolean isCollectionValued() {
        return collectionValued;
    }

    public void setCollectionValued(boolean collectionValued) {
        this.collectionValued = collectionValued;
    }

    @Override
    public String toString() {
        if (!collectionValued && value instanceof Enum<?>) {
            Enum value = (Enum) this.value;
            return value.getDeclaringClass().getName() + "." + value.name();
        } else {
            if (Character.isDigit(name.charAt(0))) {
                return "?" + name;
            } else {
                return ":" + name;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ParameterExpression)) {
            return false;
        }

        ParameterExpression that = (ParameterExpression) o;

        if (collectionValued != that.collectionValued) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        return value != null ? value.equals(that.value) : that.value == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (collectionValued ? 1 : 0);
        return result;
    }
}
