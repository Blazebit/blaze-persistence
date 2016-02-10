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
public class LikePredicate extends NegatableBinaryExpressionPredicate {

    private final boolean caseSensitive;
    private final Character escapeCharacter;

    public LikePredicate(Expression left, Expression right, boolean caseSensitive, Character escapeCharacter) {
        super(left, right);
        this.caseSensitive = caseSensitive;
        this.escapeCharacter = escapeCharacter;
    }

    public LikePredicate(Expression left, Expression right, boolean caseSensitive, Character escapeCharacter, boolean negated) {
        super(left, right, negated);
        this.caseSensitive = caseSensitive;
        this.escapeCharacter = escapeCharacter;
    }

    @Override
    public LikePredicate clone() {
        return new LikePredicate(left.clone(), right.clone(), caseSensitive, escapeCharacter, negated);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public Character getEscapeCharacter() {
        return escapeCharacter;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.caseSensitive ? 1 : 0);
        hash = 89 * hash + (this.escapeCharacter != null ? this.escapeCharacter.hashCode() : 0);
        hash = 89 * hash + super.hashCode();
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
        final LikePredicate other = (LikePredicate) obj;
        if (this.caseSensitive != other.caseSensitive) {
            return false;
        }
        if (this.escapeCharacter != other.escapeCharacter && (this.escapeCharacter == null || !this.escapeCharacter.equals(other.escapeCharacter))) {
            return false;
        }
        return super.equals(obj);
    }

}
