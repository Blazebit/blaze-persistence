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
public class EqPredicate extends QuantifiableBinaryExpressionPredicate {

    // This field indicates whether it is necessary to include this KEY(x) = y clause in the
    // where clause. E.g. this is not necessary for id and count query if the corresponding map access
    // only occurs in the select clause.
    private boolean requiredByMapValueSelect = false;

    public EqPredicate(Expression left, Expression right) {
        super(left, right, PredicateQuantifier.ONE);
    }

    public EqPredicate(Expression left, Expression right, PredicateQuantifier quantifier) {
        super(left, right, quantifier);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public static class EqPredicateBuilder<T> extends AbstractQuantifiablePredicateBuilder<T> {

        public EqPredicateBuilder(T result, PredicateBuilderEndedListener listener, Expression leftExpression, boolean wrapNot, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory) {
            super(result, listener, leftExpression, wrapNot, subqueryInitFactory, expressionFactory);
        }

        @Override
        public T value(Object value) {
            return chain(new EqPredicate(leftExpression, new ParameterExpression(value), PredicateQuantifier.ONE));
        }

        @Override
        public T expression(String expression) {
            return chain(new EqPredicate(leftExpression, expressionFactory.createSimpleExpression(expression),
                                         PredicateQuantifier.ONE));
        }

        @Override
        public SubqueryInitiator<T> all() {
            chainSubquery(new EqPredicate(leftExpression, null, PredicateQuantifier.ALL));
            return super.all();
        }

        @Override
        public SubqueryInitiator<T> any() {
            chainSubquery(new EqPredicate(leftExpression, null, PredicateQuantifier.ANY));
            return super.any();
        }

        @Override
        public SubqueryBuilder<T> from(Class<?> clazz) {
            chainSubquery(new EqPredicate(leftExpression, null, PredicateQuantifier.ONE));
            return getSubqueryInitiator().from(clazz);
        }

        @Override
        public SubqueryBuilder<T> from(Class<?> clazz, String alias) {
            chainSubquery(new EqPredicate(leftExpression, null, PredicateQuantifier.ONE));
            return getSubqueryInitiator().from(clazz, alias);
        }

        @Override
        public SubqueryInitiator<T> all(String subqueryAlias, String expression) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public SubqueryInitiator<T> any(String subqueryAlias, String expression) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    public boolean isRequiredByMapValueSelect() {
        return requiredByMapValueSelect;
    }

    public void setRequiredByMapValueSelect(boolean requiredByMapValueSelect) {
        this.requiredByMapValueSelect = requiredByMapValueSelect;
    }
}
