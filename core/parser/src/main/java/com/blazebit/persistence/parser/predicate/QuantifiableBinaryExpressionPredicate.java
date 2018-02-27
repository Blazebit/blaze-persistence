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

package com.blazebit.persistence.parser.predicate;

import com.blazebit.persistence.parser.expression.Expression;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public abstract class QuantifiableBinaryExpressionPredicate extends BinaryExpressionPredicate {

    protected PredicateQuantifier quantifier;

    public QuantifiableBinaryExpressionPredicate() {
        this(null, null, false);
    }

    public QuantifiableBinaryExpressionPredicate(Expression left, Expression right, boolean negated) {
        this(left, right, PredicateQuantifier.ONE, negated);
    }

    public QuantifiableBinaryExpressionPredicate(Expression left, Expression right, PredicateQuantifier quantifier, boolean negated) {
        super(left, right, negated);
        this.quantifier = quantifier;
    }

    @Override
    public abstract QuantifiableBinaryExpressionPredicate clone(boolean resolved);

    public PredicateQuantifier getQuantifier() {
        return quantifier;
    }
    
    
    public void setQuantifier(PredicateQuantifier quantifier) {
        this.quantifier = quantifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QuantifiableBinaryExpressionPredicate)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        QuantifiableBinaryExpressionPredicate that = (QuantifiableBinaryExpressionPredicate) o;

        return quantifier == that.quantifier;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (quantifier != null ? quantifier.hashCode() : 0);
        return result;
    }
}
