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
package com.blazebit.persistence.impl.predicate;

import com.blazebit.persistence.impl.expression.Expression;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public abstract class QuantifiableBinaryExpressionPredicate extends BinaryExpressionPredicate {

    private final PredicateQuantifier quantifier;

    public QuantifiableBinaryExpressionPredicate() {
        this(null, null);
    }

    public QuantifiableBinaryExpressionPredicate(Expression left, Expression right) {
        this(left, right, PredicateQuantifier.ONE);
    }

    public QuantifiableBinaryExpressionPredicate(Expression left, Expression right, PredicateQuantifier quantifier) {
        super(left, right);
        this.quantifier = quantifier;
    }

    public PredicateQuantifier getQuantifier() {
        return quantifier;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.quantifier != null ? this.quantifier.hashCode() : 0);
        hash = 97 * hash + super.hashCode();
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
        final QuantifiableBinaryExpressionPredicate other = (QuantifiableBinaryExpressionPredicate) obj;
        if (this.quantifier != other.quantifier) {
            return false;
        }
        return super.equals(obj);
    }
}
