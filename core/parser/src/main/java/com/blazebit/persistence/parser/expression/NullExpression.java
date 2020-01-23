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
 * @since 1.0.1
 */
public class NullExpression extends AbstractExpression implements LiteralExpression<Object> {

    public static final NullExpression INSTANCE = new NullExpression();

    @Override
    public NullExpression copy() {
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
