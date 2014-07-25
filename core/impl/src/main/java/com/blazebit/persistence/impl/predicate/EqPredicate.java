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

import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.Expressions;
import com.blazebit.persistence.impl.expression.ParameterExpression;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 */
public class EqPredicate  extends QuantifiableBinaryExpressionPredicate {
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

        public EqPredicateBuilder(T result, PredicateBuilderEndedListener listener, Expression leftExpression, boolean wrapNot, SubqueryInitiatorFactory subqueryInitFactory) {
            super(result, listener, leftExpression, wrapNot, subqueryInitFactory);
        }
       
        @Override
        public T value(Object value) {
            return chain(new EqPredicate(leftExpression, new ParameterExpression(value), quantifier));
        }

        @Override
        public T expression(String expression) {
            return chain(new EqPredicate(leftExpression, Expressions.createSimpleExpression(expression), quantifier));
        }
        
    }

    public boolean isRequiredByMapValueSelect() {
        return requiredByMapValueSelect;
    }

    public void setRequiredByMapValueSelect(boolean requiredByMapValueSelect) {
        this.requiredByMapValueSelect = requiredByMapValueSelect;
    }
}
