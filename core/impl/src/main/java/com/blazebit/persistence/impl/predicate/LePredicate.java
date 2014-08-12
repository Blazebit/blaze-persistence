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

import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.ParameterExpression;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class LePredicate extends QuantifiableBinaryExpressionPredicate {

    public LePredicate(Expression left, Expression right) {
        super(left, right, PredicateQuantifier.ONE);
    }

    public LePredicate(Expression left, Expression right, PredicateQuantifier quantifier) {
        super(left, right, quantifier);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public static class LePredicateBuilder<T> extends AbstractQuantifiablePredicateBuilder<T> {

        public LePredicateBuilder(T result, PredicateBuilderEndedListener listener, Expression leftExpression, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory) {
            super(result, listener, leftExpression, false, subqueryInitFactory, expressionFactory);
        }

        @Override
        protected QuantifiableBinaryExpressionPredicate createPredicate(Expression left, Expression right, PredicateQuantifier quantifier){
            return new LePredicate(left, right, quantifier);
        }
    }
}
