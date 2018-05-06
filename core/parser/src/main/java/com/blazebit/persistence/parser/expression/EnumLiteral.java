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
public class EnumLiteral extends AbstractExpression implements LiteralExpression<Enum<?>> {

    private final Enum<?> value;
    private final String originalExpression;

    public EnumLiteral(Enum<?> value, String originalExpression) {
        this.value = value;
        this.originalExpression = originalExpression;
    }

    @Override
    public Enum<?> getValue() {
        return value;
    }

    public String getOriginalExpression() {
        return originalExpression;
    }

    @Override
    public Expression clone(boolean resolved) {
        return new EnumLiteral(value, originalExpression);
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
        if (!(o instanceof EnumLiteral)) {
            return false;
        }

        EnumLiteral that = (EnumLiteral) o;

        return value != null ? value.equals(that.value) : that.value == null;

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return originalExpression;
    }
}
