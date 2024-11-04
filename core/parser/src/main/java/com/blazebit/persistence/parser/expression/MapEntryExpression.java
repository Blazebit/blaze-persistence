/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser.expression;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MapEntryExpression extends AbstractExpression implements PathElementExpression, QualifiedExpression {

    private PathExpression path;

    public MapEntryExpression(PathExpression path) {
        this.path = path;
    }

    @Override
    public MapEntryExpression copy(ExpressionCopyContext copyContext) {
        return new MapEntryExpression((PathExpression) path.copy(copyContext));
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
    public PathExpression getPath() {
        return path;
    }

    @Override
    public String getQualificationExpression() {
        return "ENTRY";
    }

    public void setPath(PathExpression path) {
        this.path = path;
    }

    @Override
    public int hashCode() {
        return getPath() != null ? getPath().hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (getClass() != o.getClass()) {
            return false;
        }

        MapEntryExpression that = (MapEntryExpression) o;

        return getPath() != null ? getPath().equals(that.getPath()) : that.getPath() == null;
    }

}
