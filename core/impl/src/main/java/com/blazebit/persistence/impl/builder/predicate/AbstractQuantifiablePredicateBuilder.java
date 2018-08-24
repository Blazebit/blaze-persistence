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

package com.blazebit.persistence.impl.builder.predicate;

import com.blazebit.persistence.CaseWhenAndThenBuilder;
import com.blazebit.persistence.CaseWhenBuilder;
import com.blazebit.persistence.CaseWhenOrThenBuilder;
import com.blazebit.persistence.CaseWhenThenBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.LeafOngoingFinalSetOperationSubqueryBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.QuantifiableBinaryPredicateBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SimpleCaseWhenBuilder;
import com.blazebit.persistence.StartOngoingSetOperationSubqueryBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.MultipleSubqueryInitiatorImpl;
import com.blazebit.persistence.impl.ParameterManager;
import com.blazebit.persistence.impl.SubqueryAndExpressionBuilderListener;
import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.impl.SubqueryInternalBuilder;
import com.blazebit.persistence.impl.builder.expression.CaseWhenBuilderImpl;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListener;
import com.blazebit.persistence.impl.builder.expression.SimpleCaseWhenBuilderImpl;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.parser.predicate.BinaryExpressionPredicate;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.parser.predicate.PredicateBuilder;
import com.blazebit.persistence.parser.predicate.PredicateQuantifier;
import com.blazebit.persistence.parser.predicate.QuantifiableBinaryExpressionPredicate;

import java.util.Collection;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public abstract class AbstractQuantifiablePredicateBuilder<T> extends SubqueryAndExpressionBuilderListener<T> implements QuantifiableBinaryPredicateBuilder<T>, PredicateBuilder {

    protected final Expression leftExpression;
    protected final SubqueryInitiatorFactory subqueryInitFactory;
    protected final ExpressionFactory expressionFactory;

    private final T result;
    private final PredicateBuilderEndedListener listener;
    private final boolean wrapNot;
    private Predicate predicate;
    private final ParameterManager parameterManager;
    private final ClauseType clauseType;
    private SubqueryInitiator<T> subqueryInitiator;

    public AbstractQuantifiablePredicateBuilder(T result, PredicateBuilderEndedListener listener, Expression leftExpression, boolean wrapNot, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory, ParameterManager parameterManager, ClauseType clauseType) {
        this.result = result;
        this.listener = listener;
        this.wrapNot = wrapNot;
        this.leftExpression = leftExpression;
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
        this.parameterManager = parameterManager;
        this.clauseType = clauseType;
    }

    protected abstract QuantifiableBinaryExpressionPredicate createPredicate(Expression left, Expression right, PredicateQuantifier quantifier);

    protected T chain(Predicate predicate) {
        verifyBuilderEnded();
        if (wrapNot) {
            predicate.negate();
        }
        this.predicate = predicate;
        listener.onBuilderEnded(this);
        return result;
    }

    protected void chainSubbuilder(Predicate predicate) {
        verifyBuilderEnded();
        if (wrapNot) {
            predicate.negate();
        }
        this.predicate = predicate;
    }

    @Override
    public T value(Object value) {
        return chain(createPredicate(leftExpression, parameterManager.addParameterExpression(value, clauseType, subqueryInitFactory.getQueryBuilder()), PredicateQuantifier.ONE));
    }

    @Override
    public T expression(String expression) {
        return chain(createPredicate(leftExpression, expressionFactory.createSimpleExpression(expression, false), PredicateQuantifier.ONE));
    }

    @Override
    public MultipleSubqueryInitiator<T> subqueries(String expression) {
        return new MultipleSubqueryInitiatorImpl<T>(result, expressionFactory.createSimpleExpression(expression, true), new ExpressionBuilderEndedListener() {
            
            @Override
            public void onBuilderEnded(ExpressionBuilder builder) {
                chain(createPredicate(leftExpression, builder.getExpression(), PredicateQuantifier.ONE));
            }
            
        }, subqueryInitFactory, clauseType);
    }

    /* case when functions */
    @Override
    public RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>> caseWhen(String expression) {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return startBuilder(new CaseWhenBuilderImpl<T>(result, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType)).when(expression);
    }

    @Override
    public CaseWhenAndThenBuilder<CaseWhenBuilder<T>> caseWhenAnd() {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return startBuilder(new CaseWhenBuilderImpl<T>(result, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType)).whenAnd();
    }

    @Override
    public SubqueryInitiator<CaseWhenThenBuilder<CaseWhenBuilder<T>>> caseWhenExists() {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return startBuilder(new CaseWhenBuilderImpl<T>(result, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType)).whenExists();
    }

    @Override
    public SubqueryInitiator<CaseWhenThenBuilder<CaseWhenBuilder<T>>> caseWhenNotExists() {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return startBuilder(new CaseWhenBuilderImpl<T>(result, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType)).whenNotExists();
    }

    @Override
    public SubqueryBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>> caseWhenExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return startBuilder(new CaseWhenBuilderImpl<T>(result, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType)).whenExists(criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>> caseWhenNotExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return startBuilder(new CaseWhenBuilderImpl<T>(result, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType)).whenNotExists(criteriaBuilder);
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> caseWhenSubquery() {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return startBuilder(new CaseWhenBuilderImpl<T>(result, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType)).whenSubquery();
    }

    @Override
    public SubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> caseWhenSubquery(String subqueryAlias, String expression) {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return startBuilder(new CaseWhenBuilderImpl<T>(result, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType)).whenSubquery(subqueryAlias, expression);
    }

    @Override
    public MultipleSubqueryInitiator<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> caseWhenSubqueries(String expression) {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return startBuilder(new CaseWhenBuilderImpl<T>(result, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType)).whenSubqueries(expression);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> caseWhenSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return startBuilder(new CaseWhenBuilderImpl<T>(result, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType)).whenSubquery(criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<RestrictionBuilder<CaseWhenThenBuilder<CaseWhenBuilder<T>>>> caseWhenSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return startBuilder(new CaseWhenBuilderImpl<T>(result, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType)).whenSubquery(subqueryAlias, expression, criteriaBuilder);
    }

    @Override
    public CaseWhenOrThenBuilder<CaseWhenBuilder<T>> caseWhenOr() {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return startBuilder(new CaseWhenBuilderImpl<T>(result, this, subqueryInitFactory, expressionFactory, parameterManager, clauseType)).whenOr();
    }

    @Override
    public SimpleCaseWhenBuilder<T> simpleCase(String caseOperand) {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return startBuilder(new SimpleCaseWhenBuilderImpl<T>(result, this, expressionFactory, expressionFactory.createCaseOperandExpression(caseOperand)));
    }

    /* quantification functions */
    @Override
    public SubqueryInitiator<T> all() {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ALL));
        return subqueryInitFactory.createSubqueryInitiator(result, this, false, clauseType);
    }

    @Override
    public SubqueryInitiator<T> any() {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ANY));
        return subqueryInitFactory.createSubqueryInitiator(result, this, false, clauseType);
    }

    @Override
    public SubqueryBuilder<T> all(FullQueryBuilder<?, ?> criteriaBuilder) {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ALL));
        return subqueryInitFactory.createSubqueryBuilder(result, this, false, criteriaBuilder, clauseType);
    }

    @Override
    public SubqueryBuilder<T> any(FullQueryBuilder<?, ?> criteriaBuilder) {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ANY));
        return subqueryInitFactory.createSubqueryBuilder(result, this, false, criteriaBuilder, clauseType);
    }

    public SubqueryBuilder<T> one(FullQueryBuilder<?, ?> criteriaBuilder) {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return subqueryInitFactory.createSubqueryBuilder(result, this, false, criteriaBuilder, clauseType);
    }

    @Override
    public SubqueryBuilder<T> from(Class<?> clazz) {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return getSubqueryInitiator().from(clazz);
    }

    @Override
    public SubqueryBuilder<T> from(Class<?> clazz, String alias) {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return getSubqueryInitiator().from(clazz, alias);
    }

    @Override
    public SubqueryBuilder<T> from(String correlationPath) {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return getSubqueryInitiator().from(correlationPath);
    }

    @Override
    public SubqueryBuilder<T> from(String correlationPath, String alias) {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return getSubqueryInitiator().from(correlationPath, alias);
    }

    @Override
    public StartOngoingSetOperationSubqueryBuilder<T, LeafOngoingFinalSetOperationSubqueryBuilder<T>> startSet() {
        return getSubqueryInitiator().startSet();
    }

    @Override
    public SubqueryBuilder<T> fromOld(Class<?> entityClass) {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return getSubqueryInitiator().fromOld(entityClass);
    }

    @Override
    public SubqueryBuilder<T> fromOld(Class<?> entityClass, String alias) {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return getSubqueryInitiator().fromOld(entityClass, alias);
    }

    @Override
    public SubqueryBuilder<T> fromNew(Class<?> entityClass) {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return getSubqueryInitiator().fromNew(entityClass);
    }

    @Override
    public SubqueryBuilder<T> fromNew(Class<?> entityClass, String alias) {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return getSubqueryInitiator().fromNew(entityClass, alias);
    }

    @Override
    public SubqueryBuilder<T> fromValues(Class<?> valueClass, String alias, int valueCount) {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return getSubqueryInitiator().fromValues(valueClass, alias, valueCount);
    }

    @Override
    public SubqueryBuilder<T> fromIdentifiableValues(Class<?> valueClass, String alias, int valueCount) {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return getSubqueryInitiator().fromIdentifiableValues(valueClass, alias, valueCount);
    }

    @Override
    public <X> SubqueryBuilder<T> fromValues(Class<X> valueClass, String alias, Collection<X> values) {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return getSubqueryInitiator().fromValues(valueClass, alias, values);
    }

    @Override
    public <X> SubqueryBuilder<T> fromIdentifiableValues(Class<X> valueClass, String alias, Collection<X> values) {
        chainSubbuilder(createPredicate(leftExpression, null, PredicateQuantifier.ONE));
        return getSubqueryInitiator().fromIdentifiableValues(valueClass, alias, values);
    }

    @Override
    public void onBuilderEnded(SubqueryInternalBuilder<T> builder) {
        super.onBuilderEnded(builder);
        // set the finished subquery builder on the previously created predicate
        if (predicate instanceof BinaryExpressionPredicate) {
            ((BinaryExpressionPredicate) predicate).setRight(new SubqueryExpression(builder));
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
            subqueryInitiator = subqueryInitFactory.createSubqueryInitiator(result, this, false, clauseType);
        }
        return subqueryInitiator;
    }

    @Override
    public void onBuilderEnded(ExpressionBuilder builder) {
        super.onBuilderEnded(builder);
        if (predicate instanceof BinaryExpressionPredicate) {
            ((BinaryExpressionPredicate) predicate).setRight(builder.getExpression());
        } else {
            throw new IllegalStateException("ExpressionBuilder ended but predicate type was unexpected");
        }

        listener.onBuilderEnded(AbstractQuantifiablePredicateBuilder.this);
    }
}
