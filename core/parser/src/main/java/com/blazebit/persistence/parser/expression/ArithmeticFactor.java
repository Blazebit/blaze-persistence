/*
 * Copyright 2014 - 2018 Blazebit.
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
 * @author Moritz Becker
 * @since 1.2.0
 */
public class ArithmeticFactor extends AbstractNumericExpression {

    private Expression expression;
    private boolean invertSignum;

    public ArithmeticFactor(Expression expression, boolean invertSignum) {
        super(expression instanceof NumericExpression ? ((NumericExpression) expression).getNumericType() : null);
        this.expression = expression;
        this.invertSignum = invertSignum;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public boolean isInvertSignum() {
        return invertSignum;
    }

    public void setInvertSignum(boolean invertSignum) {
        this.invertSignum = invertSignum;
    }

    @Override
    public Expression clone(boolean resolved) {
        return new ArithmeticFactor(expression.clone(resolved), invertSignum);
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ArithmeticFactor)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ArithmeticFactor that = (ArithmeticFactor) o;

        if (invertSignum != that.invertSignum) {
            return false;
        }
        return expression != null ? expression.equals(that.expression) : that.expression == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (expression != null ? expression.hashCode() : 0);
        result = 31 * result + (invertSignum ? 1 : 0);
        return result;
    }
}
