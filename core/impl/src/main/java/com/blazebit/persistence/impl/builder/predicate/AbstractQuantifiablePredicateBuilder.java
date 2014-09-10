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
package com.blazebit.persistence.impl.builder.predicate;

import com.blazebit.persistence.CaseWhenAndThenBuilder;
import com.blazebit.persistence.CaseWhenBuilder;
import com.blazebit.persistence.CaseWhenOrThenBuilder;
import com.blazebit.persistence.CaseWhenThenBuilder;
import com.blazebit.persistence.QuantifiableBinaryPredicateBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.SubqueryBuilderImpl;
import com.blazebit.persistence.impl.SubqueryBuilderListenerImpl;
import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.predicate.BinaryExpressionPredicate;
import com.blazebit.persistence.impl.predicate.NotPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;
import com.blazebit.persistence.impl.predicate.PredicateQuantifier;
import com.blazebit.persistence.impl.predicate.QuantifiableBinaryExpressionPredicate;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public abstract class AbstractQuantifiablePredicateBuilder<T> extends SubqueryBuilderListenerImpl<T> implements QuantifiableBinaryPredicateBuilder<T>, PredicateBuilder {

    private final T result;
    private final PredicateBuilderEndedListener listener;
    private final boolean wrapNot;
    protected final Expression leftExpression;
    protected final SubqueryInitiatorFactory subqueryInitFactory;
    private Predicate predicate;
    protected final ExpressionFactory expressionFactory;
    private SubqueryInitiator<T> subqueryInitiator;

    public AbstractQuantifiablePredicateBuilder(T result, PredicateBuilderEndedListener listener, Expression leftExpression, boolean wrapNot, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory) {
        this.result = result;
        this.listener = listener;
        this.wrapNot = wrapNot;
        this.leftExpression = leftExpression;
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
    }

    protected abstract QuantifiableBinaryExpressionPredicate createPredicate(Expression left, Expression right, PredicateQuantifier quantifier);

    protected T chain(Predicate predicate) {
        verifySubqueryBuilderEnded();
        this.predicate = wrapNot ? new NotPredicate(predicate) : predicate;
        listener.onBuilderEnded(this);
        return result;
    }

    protected void chainSubquery(Predicate predicate) {
        verifySubqueryBuilderEnded();
        this.predicate = wrapNot ? new NotPredicate(predicate) : predicate;
    }

    @Override
    public T value(Object value) {
        return chain(createPredicate(leftExpression, new ParameterExpression(value), PredicateQuantifier.ONE));
    }

    @Override
    public T expression(String expression) {
        return chain(createPredicate(leftExpression, expressionFactory.createSimpleExpression(expression),
                PredicateQuantifier.ONE));
    }
    
    /* case when functions */
    
    @Override
    public RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>> caseWhen(String expression) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CaseWhenAndThenBuilder<CaseWhenBuilder<T>> caseWhenAnd() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SubqueryInitiator<CaseWhenThenBuilder<CaseWhenBuilder<T>>> caseWhenExists() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> caseWhenSubquery() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> caseWhenSubquery(String subqueryAlias, String expression) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SubqueryInitiator<CaseWhenThenBuilder<CaseWhenBuilder<T>>> caseWhenNotExists() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CaseWhenOrThenBuilder<CaseWhenBuilder<T>> caseWhenOr() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /* quantification functions */

    @Override
    public SubqueryInitiator<T> all() {
        chainSubquery(createPredicate(leftExpression, null, PredicateQuantifier.ALL));
        return subqueryInitFactory.createSubqueryInitiator(result, this);
    }

    @Override
    public SubqueryInitiator<T> any() {
        chainSubquery(createPredicate(leftExpression, null, PredicateQuantifier.ANY));
        return subqueryInitFactory.createSubqueryInitiator(result, this);
    }

    @Override
    public SubqueryBuilder<T> from(Class<?> clazz) {
        chainSubquery(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return getSubqueryInitiator().from(clazz);
    }

    @Override
    public SubqueryBuilder<T> from(Class<?> clazz, String alias) {
        chainSubquery(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return getSubqueryInitiator().from(clazz, alias);
    }

    @Override
    public void onBuilderEnded(SubqueryBuilderImpl<T> builder) {
        super.onBuilderEnded(builder);
        // set the finished subquery builder on the previously created predicate
        Predicate pred;
        if (predicate instanceof NotPredicate) {
            // unwrap not predicate
            pred = ((NotPredicate) predicate).getPredicate();
        } else {
            pred = predicate;
        }

        if (pred instanceof BinaryExpressionPredicate) {
            ((BinaryExpressionPredicate) pred).setRight(new SubqueryExpression(builder));
        } else {
            throw new IllegalStateException("SubqueryBuilder ended but predicate type was unexpected");
        }

        listener.onBuilderEnded(this);
    }

    @Override
    public Predicate getPredicate() {
        return predicate;
    }

    protected SubqueryInitiator<T> getSubqueryInitiator() {
        if (subqueryInitiator == null) {
            subqueryInitiator = subqueryInitFactory.createSubqueryInitiator(result, this);
        }
        return subqueryInitiator;
    }
}
