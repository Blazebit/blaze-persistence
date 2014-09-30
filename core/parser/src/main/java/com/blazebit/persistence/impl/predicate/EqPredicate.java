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

import com.blazebit.persistence.impl.SimpleQueryGenerator;
import com.blazebit.persistence.impl.expression.Expression;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class EqPredicate extends QuantifiableBinaryExpressionPredicate implements Negatable {
    private boolean negated;

    public EqPredicate(boolean negated) {
        this.negated = negated;
    }
    
    public EqPredicate(Expression left, Expression right) {
        this(left, right, PredicateQuantifier.ONE, false);
    }
    
    public EqPredicate(Expression left, Expression right, boolean negated) {
        super(left, right, PredicateQuantifier.ONE);
        this.negated = negated;
    }

    public EqPredicate(Expression left, Expression right, PredicateQuantifier quantifier) {
        this(left, right, quantifier, false);
    }
    
    public EqPredicate(Expression left, Expression right, PredicateQuantifier quantifier, boolean negated) {
        super(left, right, quantifier);
        this.negated = negated;
    }

    @Override
    public boolean isNegated() {
        return negated;
    }

    @Override
    public void setNegated(boolean negated) {
        this.negated = negated;
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
    public int hashCode() {
        int hash = super.hashCode();
        hash = 97 * hash + (this.negated ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj)){
            return false;
        }
        final EqPredicate other = (EqPredicate) obj;
        if (this.negated != other.negated) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        SimpleQueryGenerator generator = new SimpleQueryGenerator();
        generator.setQueryBuffer(sb);
        generator.visit(this);
        return sb.toString();
    }
    
}
