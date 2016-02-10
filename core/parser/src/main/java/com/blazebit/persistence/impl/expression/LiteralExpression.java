/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence.impl.expression;

/**
 *
 * @author Christian Beikov
 * @since 1.0.1
 */
public class LiteralExpression extends AbstractExpression {

    private final String wrapperFunction;
    private final String literal;

    public LiteralExpression(String wrapperFunction, String literal) {
        this.wrapperFunction = wrapperFunction;
        this.literal = literal;
    }

    @Override
    public LiteralExpression clone() {
        return new LiteralExpression(wrapperFunction, literal);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public String getWrapperFunction() {
        return wrapperFunction;
    }

    public String getLiteral() {
        return literal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(wrapperFunction);
        sb.append('(');
        sb.append(literal);
        sb.append(')');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.wrapperFunction != null ? this.wrapperFunction.hashCode() : 0);
        hash = 67 * hash + (this.literal != null ? this.literal.hashCode() : 0);
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
        final LiteralExpression other = (LiteralExpression) obj;
        if ((this.wrapperFunction == null) ? (other.wrapperFunction != null) : !this.wrapperFunction.equals(other.wrapperFunction)) {
            return false;
        }
        if ((this.literal == null) ? (other.literal != null) : !this.literal.equals(other.literal)) {
            return false;
        }
        return true;
    }

}
