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
 * @author Moritz Becker
 * @since 1.0
 */
public class FooExpression implements Expression {

    private final StringBuilder stringBuilder;

    public FooExpression(CharSequence string) {
        this.stringBuilder = new StringBuilder(string);
    }
    
    public FooExpression(StringBuilder sb) {
        this.stringBuilder = sb;
    }

    @Override
    public FooExpression clone() {
        return new FooExpression(stringBuilder);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

    StringBuilder getStringBuilder() {
        return stringBuilder;
    }
    
    
    @Override
    public String toString() {
        return stringBuilder.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + (this.stringBuilder.toString() != null ? this.stringBuilder.toString().hashCode() : 0);
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
        final FooExpression other = (FooExpression) obj;
        if (this.stringBuilder != other.stringBuilder && (this.stringBuilder == null || !this.stringBuilder.toString().equals(other.stringBuilder.toString()))) {
            return false;
        }
        return true;
    }

}
