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
public class PropertyExpression extends AbstractExpression implements PathElementExpression {

    private final String property;

    public PropertyExpression(String property) {
        this.property = property;
    }

    @Override
    public PropertyExpression copy(ExpressionCopyContext copyContext) {
        // We can do this since this is immutable
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

    public String getProperty() {
        return property;
    }

    @Override
    public String toString() {
        return property;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.property != null ? this.property.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PropertyExpression other = (PropertyExpression) obj;
        if ((this.property == null) ? (other.property != null) : !this.property.equals(other.property)) {
            return false;
        }
        return true;
    }

}
