/*
 * Copyright 2014 - 2023 Blazebit.
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
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;
import com.blazebit.persistence.parser.expression.StringLiteral;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class LikePredicate extends BinaryExpressionPredicate {

    private final boolean caseSensitive;
    private Expression escapeCharacter;

    public LikePredicate(Expression left, Expression right, boolean caseSensitive, Character escapeCharacter) {
        this(left, right, caseSensitive, escapeCharacter == null ? null : new StringLiteral(escapeCharacter.toString()), false);
    }

    public LikePredicate(Expression left, Expression right, boolean caseSensitive, Expression escapeCharacter) {
        this(left, right, caseSensitive, escapeCharacter, false);
    }

    public LikePredicate(Expression left, Expression right, boolean caseSensitive, Character escapeCharacter, boolean negated) {
        this(left, right, caseSensitive, escapeCharacter == null ? null : new StringLiteral(escapeCharacter.toString()), negated);
    }

    public LikePredicate(Expression left, Expression right, boolean caseSensitive, Expression escapeCharacter, boolean negated) {
        super(left, right, negated);
        this.caseSensitive = caseSensitive;
        this.escapeCharacter = escapeCharacter;
    }

    @Override
    public LikePredicate copy(ExpressionCopyContext copyContext) {
        return new LikePredicate(left.copy(copyContext), right.copy(copyContext), caseSensitive, escapeCharacter, negated);
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

    public Expression getEscapeCharacter() {
        return escapeCharacter;
    }

    public void setEscapeCharacter(Expression escapeCharacter) {
        this.escapeCharacter = escapeCharacter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LikePredicate)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        LikePredicate that = (LikePredicate) o;

        if (caseSensitive != that.caseSensitive) {
            return false;
        }
        return escapeCharacter != null ? escapeCharacter.equals(that.escapeCharacter) : that.escapeCharacter == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (caseSensitive ? 1 : 0);
        result = 31 * result + (escapeCharacter != null ? escapeCharacter.hashCode() : 0);
        return result;
    }
}
