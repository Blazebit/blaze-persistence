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
    public ParameterExpression clone(boolean resolved) {
        return new ParameterExpression(name, value, collectionValued);
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
        if (Character.isDigit(name.charAt(0))) {
            return "?" + name;
        } else {
            return ":" + name;
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
