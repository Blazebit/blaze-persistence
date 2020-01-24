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

package com.blazebit.persistence.parser.expression;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public final class OrderByItem {

    private final boolean ascending;
    private final boolean nullFirst;
    private Expression expression;

    public OrderByItem(boolean ascending, boolean nullFirst, Expression expression) {
        this.ascending = ascending;
        this.nullFirst = nullFirst;
        this.expression = expression;
    }

    public OrderByItem copy(ExpressionCopyContext copyContext) {
        return new OrderByItem(ascending, nullFirst, expression.copy(copyContext));
    }

    public boolean isAscending() {
        return ascending;
    }

    public boolean isDescending() {
        return !ascending;
    }

    public boolean isNullFirst() {
        return nullFirst;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.ascending ? 1 : 0);
        hash = 37 * hash + (this.nullFirst ? 1 : 0);
        hash = 37 * hash + (this.expression != null ? this.expression.hashCode() : 0);
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
        final OrderByItem other = (OrderByItem) obj;
        if (this.ascending != other.ascending) {
            return false;
        }
        if (this.nullFirst != other.nullFirst) {
            return false;
        }
        if (this.expression != other.expression && (this.expression == null || !this.expression.equals(other.expression))) {
            return false;
        }
        return true;
    }
}
