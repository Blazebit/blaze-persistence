/*
 * Copyright 2014 - 2022 Blazebit.
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

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class IsNullPredicate extends UnaryExpressionPredicate {

    public IsNullPredicate(Expression expression) {
        super(expression);
    }

    public IsNullPredicate(Expression expression, boolean negated) {
        super(expression, negated);
    }

    @Override
    public IsNullPredicate copy(ExpressionCopyContext copyContext) {
        return new IsNullPredicate(expression.copy(copyContext), negated);
    }

    @Override
    public void accept(Expression.Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(Expression.ResultVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
