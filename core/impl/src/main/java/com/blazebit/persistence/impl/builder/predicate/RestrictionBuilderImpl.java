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

import com.blazebit.persistence.BetweenBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.LikeBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.QuantifiableBinaryPredicateBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.ExpressionUtils;
import com.blazebit.persistence.impl.MultipleSubqueryInitiatorImpl;
import com.blazebit.persistence.impl.ParameterManager;
import com.blazebit.persistence.impl.PredicateAndSubqueryBuilderEndedListener;
import com.blazebit.persistence.impl.SubqueryBuilderListener;
import com.blazebit.persistence.impl.SubqueryBuilderListenerImpl;
import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.impl.SubqueryInternalBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListener;
import com.blazebit.persistence.impl.builder.expression.SuperExpressionSubqueryBuilderListener;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.parser.expression.SyntaxErrorException;
import com.blazebit.persistence.parser.predicate.BinaryExpressionPredicate;
import com.blazebit.persistence.parser.predicate.EqPredicate;
import com.blazebit.persistence.parser.predicate.GePredicate;
import com.blazebit.persistence.parser.predicate.GtPredicate;
import com.blazebit.persistence.parser.predicate.InPredicate;
import com.blazebit.persistence.parser.predicate.IsEmptyPredicate;
import com.blazebit.persistence.parser.predicate.IsNullPredicate;
import com.blazebit.persistence.parser.predicate.LePredicate;
import com.blazebit.persistence.parser.predicate.LtPredicate;
import com.blazebit.persistence.parser.predicate.MemberOfPredicate;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.parser.predicate.PredicateBuilder;
import com.blazebit.persistence.internal.RestrictionBuilderExperimental;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class RestrictionBuilderImpl<T> extends PredicateAndSubqueryBuilderEndedListener<T> implements RestrictionBuilderExperimental<T>, LeftHandsideSubqueryPredicateBuilder {

    private final T result;
    private final PredicateBuilderEndedListener listener;
    private Expression leftExpression;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private Predicate predicate;
    private final ExpressionFactory expressionFactory;
    private final ParameterManager parameterManager;
    private final ClauseType clause;
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final SubqueryBuilderListenerImpl<BetweenBuilder<T>> betweenStartSubqueryBuilderListener = new LeftHandsideSubqueryPredicateBuilderListener();
    private SubqueryBuilderListenerImpl<T> rightSuperExprSubqueryBuilderListener;
    private SubqueryBuilderListenerImpl<BetweenBuilder<T>> leftSuperExprSubqueryPredicateBuilderListener;
    private SuperExpressionRightHandsideSubqueryPredicateBuilder rightSuperExprSubqueryPredicateBuilderListener;

    public RestrictionBuilderImpl(T result, PredicateBuilderEndedListener listener, Expression leftExpression, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory, ParameterManager parameterManager, ClauseType clause) {
        this.leftExpression = leftExpression;
        this.listener = listener;
        this.result = result;
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
        this.parameterManager = parameterManager;
        this.clause = clause;
    }

    public RestrictionBuilderImpl(T result, PredicateBuilderEndedListener listener, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory, ParameterManager parameterManager, ClauseType clause) {
        this.listener = listener;
        this.result = result;
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
        this.parameterManager = parameterManager;
        this.clause = clause;
    }

    @Override
    public void setLeftExpression(Expression leftExpression) {
        this.leftExpression = leftExpression;
    }

    private T chain(Predicate predicate) {
        verifyBuilderEnded();
        this.predicate = predicate;
        listener.onBuilderEnded(this);
        return result;
    }

    @Override
    public Predicate getPredicate() {
        return predicate;
    }
    
    @Override
    public BetweenBuilder<T> between(Object start) {
        if (start == null) {
            throw new NullPointerException("start");
        }
        return startBuilder(new BetweenBuilderImpl<T>(result, leftExpression, parameterManager.addParameterExpression(start, clause, subqueryInitFactory.getQueryBuilder()), expressionFactory, parameterManager, this, subqueryInitFactory, clause));
    }

    @Override
    public BetweenBuilder<T> betweenExpression(String start) {
        return startBuilder(new BetweenBuilderImpl<T>(result, leftExpression, expressionFactory.createArithmeticExpression(start), expressionFactory, parameterManager, this, subqueryInitFactory, clause));
    }

    @Override
    public SubqueryInitiator<BetweenBuilder<T>> betweenSubquery() {
        BetweenBuilder<T> betweenBuilder = startBuilder(new BetweenBuilderImpl<T>(result, leftExpression, null, expressionFactory, parameterManager, this, subqueryInitFactory, clause));
        return subqueryInitFactory.createSubqueryInitiator(betweenBuilder, (SubqueryBuilderListener<BetweenBuilder<T>>) betweenStartSubqueryBuilderListener, false, clause);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public SubqueryInitiator<BetweenBuilder<T>> betweenSubquery(String subqueryAlias, String expression) {
        leftSuperExprSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression, true));
        BetweenBuilder<T> betweenBuilder = startBuilder(new BetweenBuilderImpl<T>(result, leftExpression, null, expressionFactory, parameterManager, this, subqueryInitFactory, clause));
        return subqueryInitFactory.createSubqueryInitiator(betweenBuilder, (SubqueryBuilderListener<BetweenBuilder<T>>) leftSuperExprSubqueryPredicateBuilderListener, false, clause);
    }

    @Override
    public MultipleSubqueryInitiator<BetweenBuilder<T>> betweenSubqueries(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        BetweenBuilder<T> betweenBuilder = startBuilder(new BetweenBuilderImpl<T>(result, leftExpression, expr, expressionFactory, parameterManager, this, subqueryInitFactory, clause));
        // We don't need a listener or marker here, because the resulting restriction builder can only be ended, when the initiator is ended
        return simpleMultipleBuilder(betweenBuilder, expr);
    }

    @Override
    public SubqueryBuilder<BetweenBuilder<T>> betweenSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        BetweenBuilder<T> betweenBuilder = startBuilder(new BetweenBuilderImpl<T>(result, leftExpression, null, expressionFactory, parameterManager, this, subqueryInitFactory, clause));
        return subqueryInitFactory.createSubqueryBuilder(betweenBuilder, (SubqueryBuilderListener<BetweenBuilder<T>>) betweenStartSubqueryBuilderListener, false, criteriaBuilder, clause);
    }

    @Override
    public SubqueryBuilder<BetweenBuilder<T>> betweenSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        leftSuperExprSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression, true));
        BetweenBuilder<T> betweenBuilder = startBuilder(new BetweenBuilderImpl<T>(result, leftExpression, null, expressionFactory, parameterManager, this, subqueryInitFactory, clause));
        return subqueryInitFactory.createSubqueryBuilder(betweenBuilder, (SubqueryBuilderListener<BetweenBuilder<T>>) leftSuperExprSubqueryPredicateBuilderListener, false, criteriaBuilder, clause);
    }

    private <X> MultipleSubqueryInitiator<X> simpleMultipleBuilder(X result, Expression expr) {
        MultipleSubqueryInitiator<X> initiator = new MultipleSubqueryInitiatorImpl<X>(result, expr, null, subqueryInitFactory, clause);
        return initiator;
    }
    
    private <X> MultipleSubqueryInitiator<X> simpleMultipleBuilder(X result, Expression expr, ExpressionBuilderEndedListener listener) {
        MultipleSubqueryInitiator<X> initiator = new MultipleSubqueryInitiatorImpl<X>(result, expr, listener, subqueryInitFactory, clause);
        return initiator;
    }

    @Override
    public BetweenBuilder<T> notBetween(Object start) {
        if (start == null) {
            throw new NullPointerException("start");
        }
        return startBuilder(new BetweenBuilderImpl<T>(result, leftExpression, parameterManager.addParameterExpression(start, clause, subqueryInitFactory.getQueryBuilder()), expressionFactory, parameterManager, this, subqueryInitFactory, clause, true));
    }

    @Override
    public BetweenBuilder<T> notBetweenExpression(String start) {
        return startBuilder(new BetweenBuilderImpl<T>(result, leftExpression, expressionFactory.createArithmeticExpression(start), expressionFactory, parameterManager, this, subqueryInitFactory, clause, true));
    }

    @Override
    public SubqueryInitiator<BetweenBuilder<T>> notBetweenSubquery() {
        BetweenBuilder<T> betweenBuilder = startBuilder(new BetweenBuilderImpl<T>(result, leftExpression, null, expressionFactory, parameterManager, this, subqueryInitFactory, clause, true));
        return subqueryInitFactory.createSubqueryInitiator(betweenBuilder, (SubqueryBuilderListener<BetweenBuilder<T>>) betweenStartSubqueryBuilderListener, false, clause);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public SubqueryInitiator<BetweenBuilder<T>> notBetweenSubquery(String subqueryAlias, String expression) {
        leftSuperExprSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression, true));
        BetweenBuilder<T> betweenBuilder = startBuilder(new BetweenBuilderImpl<T>(result, leftExpression, null, expressionFactory, parameterManager, this, subqueryInitFactory, clause, true));
        return subqueryInitFactory.createSubqueryInitiator(betweenBuilder, (SubqueryBuilderListener<BetweenBuilder<T>>) leftSuperExprSubqueryPredicateBuilderListener, false, clause);
    }

    @Override
    public MultipleSubqueryInitiator<BetweenBuilder<T>> notBetweenSubqueries(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        BetweenBuilder<T> betweenBuilder = startBuilder(new BetweenBuilderImpl<T>(result, leftExpression, expr, expressionFactory, parameterManager, this, subqueryInitFactory, clause, true));
        // We don't need a listener or marker here, because the resulting restriction builder can only be ended, when the initiator is ended
        return simpleMultipleBuilder(betweenBuilder, expr);
    }

    @Override
    public SubqueryBuilder<BetweenBuilder<T>> notBetweenSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        BetweenBuilder<T> betweenBuilder = startBuilder(new BetweenBuilderImpl<T>(result, leftExpression, null, expressionFactory, parameterManager, this, subqueryInitFactory, clause, true));
        return subqueryInitFactory.createSubqueryBuilder(betweenBuilder, (SubqueryBuilderListener<BetweenBuilder<T>>) betweenStartSubqueryBuilderListener, false, criteriaBuilder, clause);
    }

    @Override
    public SubqueryBuilder<BetweenBuilder<T>> notBetweenSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        leftSuperExprSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression, true));
        BetweenBuilder<T> betweenBuilder = startBuilder(new BetweenBuilderImpl<T>(result, leftExpression, null, expressionFactory, parameterManager, this, subqueryInitFactory, clause, true));
        return subqueryInitFactory.createSubqueryBuilder(betweenBuilder, (SubqueryBuilderListener<BetweenBuilder<T>>) leftSuperExprSubqueryPredicateBuilderListener, false, criteriaBuilder, clause);
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> eq() {
        return startBuilder(new EqPredicateBuilder<T>(result, this, leftExpression, false, subqueryInitFactory, expressionFactory, parameterManager, clause));
    }

    @Override
    public T eq(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return chain(new EqPredicate(leftExpression, parameterManager.addParameterExpression(value, clause, subqueryInitFactory.getQueryBuilder())));
    }

    @Override
    public T eqExpression(String expression) {
        return chain(new EqPredicate(leftExpression, expressionFactory.createSimpleExpression(expression, false)));
    }

    @Override
    public SubqueryInitiator<T> eq(String subqueryAlias, String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        rightSuperExprSubqueryPredicateBuilderListener = new SuperExpressionRightHandsideSubqueryPredicateBuilder(subqueryAlias, expr, this);
        return startBuilder(new EqPredicateBuilder<T>(result, rightSuperExprSubqueryPredicateBuilderListener, leftExpression, false, subqueryInitFactory, expressionFactory, parameterManager, clause));
    }

    @Override
    public MultipleSubqueryInitiator<T> eqSubqueries(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        return simpleMultipleBuilder(result, expr, new EqPredicateBuilder<T>(result, null, leftExpression, false, subqueryInitFactory, expressionFactory, parameterManager, clause));
    }

    @Override
    public SubqueryBuilder<T> eq(FullQueryBuilder<?, ?> criteriaBuilder) {
        return startSubqueryBuilder(new EqPredicateBuilder<T>(result, this, leftExpression, false, subqueryInitFactory, expressionFactory, parameterManager, clause), criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<T> eq(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        rightSuperExprSubqueryPredicateBuilderListener = new SuperExpressionRightHandsideSubqueryPredicateBuilder(subqueryAlias, expr, this);
        return startSubqueryBuilder(new EqPredicateBuilder<T>(result, rightSuperExprSubqueryPredicateBuilderListener, leftExpression, false, subqueryInitFactory, expressionFactory, parameterManager, clause), criteriaBuilder);
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> notEq() {
        return startBuilder(new EqPredicateBuilder<T>(result, this, leftExpression, true, subqueryInitFactory, expressionFactory, parameterManager, clause));
    }

    @Override
    public T notEq(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return chain(new EqPredicate(leftExpression, parameterManager.addParameterExpression(value, clause, subqueryInitFactory.getQueryBuilder()), true));
    }

    @Override
    public T notEqExpression(String expression) {
        return chain(new EqPredicate(leftExpression, expressionFactory.createSimpleExpression(expression, false), true));
    }

    @Override
    public SubqueryInitiator<T> notEq(String subqueryAlias, String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        rightSuperExprSubqueryPredicateBuilderListener = new SuperExpressionRightHandsideSubqueryPredicateBuilder(subqueryAlias, expr, this);
        return startBuilder(new EqPredicateBuilder<T>(result, rightSuperExprSubqueryPredicateBuilderListener, leftExpression, true, subqueryInitFactory, expressionFactory, parameterManager, clause));
    }

    @Override
    public MultipleSubqueryInitiator<T> notEqSubqueries(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        return simpleMultipleBuilder(result, expr, new EqPredicateBuilder<T>(result, null, leftExpression, true, subqueryInitFactory, expressionFactory, parameterManager, clause));
    }

    @Override
    public SubqueryBuilder<T> notEq(FullQueryBuilder<?, ?> criteriaBuilder) {
        return startSubqueryBuilder(new EqPredicateBuilder<T>(result, this, leftExpression, true, subqueryInitFactory, expressionFactory, parameterManager, clause), criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<T> notEq(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        rightSuperExprSubqueryPredicateBuilderListener = new SuperExpressionRightHandsideSubqueryPredicateBuilder(subqueryAlias, expr, this);
        return startSubqueryBuilder(new EqPredicateBuilder<T>(result, rightSuperExprSubqueryPredicateBuilderListener, leftExpression, true, subqueryInitFactory, expressionFactory, parameterManager, clause), criteriaBuilder);
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> gt() {
        return startBuilder(new GtPredicateBuilder<T>(result, this, leftExpression, subqueryInitFactory, expressionFactory, parameterManager, clause));
    }

    @Override
    public T gt(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return chain(new GtPredicate(leftExpression, parameterManager.addParameterExpression(value, clause, subqueryInitFactory.getQueryBuilder())));
    }

    @Override
    public T gtExpression(String expression) {
        return chain(new GtPredicate(leftExpression, expressionFactory.createSimpleExpression(expression, false)));
    }

    @Override
    public SubqueryInitiator<T> gt(String subqueryAlias, String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        rightSuperExprSubqueryPredicateBuilderListener = new SuperExpressionRightHandsideSubqueryPredicateBuilder(subqueryAlias, expr, this);
        return startBuilder(new GtPredicateBuilder<T>(result, rightSuperExprSubqueryPredicateBuilderListener, leftExpression, subqueryInitFactory, expressionFactory, parameterManager, clause));
    }

    @Override
    public MultipleSubqueryInitiator<T> gtSubqueries(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        return simpleMultipleBuilder(result, expr, new GtPredicateBuilder<T>(result, null, leftExpression, subqueryInitFactory, expressionFactory, parameterManager, clause));
    }

    @Override
    public SubqueryBuilder<T> gt(FullQueryBuilder<?, ?> criteriaBuilder) {
        return startSubqueryBuilder(new GtPredicateBuilder<T>(result, this, leftExpression, subqueryInitFactory, expressionFactory, parameterManager, clause), criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<T> gt(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        rightSuperExprSubqueryPredicateBuilderListener = new SuperExpressionRightHandsideSubqueryPredicateBuilder(subqueryAlias, expr, this);
        return startSubqueryBuilder(new GtPredicateBuilder<T>(result, rightSuperExprSubqueryPredicateBuilderListener, leftExpression, subqueryInitFactory, expressionFactory, parameterManager, clause), criteriaBuilder);
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> ge() {
        return startBuilder(new GePredicateBuilder<T>(result, this, leftExpression, subqueryInitFactory, expressionFactory, parameterManager, clause));
    }

    @Override
    public T ge(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return chain(new GePredicate(leftExpression, parameterManager.addParameterExpression(value, clause, subqueryInitFactory.getQueryBuilder())));
    }

    @Override
    public T geExpression(String expression) {
        return chain(new GePredicate(leftExpression, expressionFactory.createSimpleExpression(expression, false)));
    }

    @Override
    public SubqueryInitiator<T> ge(String subqueryAlias, String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        rightSuperExprSubqueryPredicateBuilderListener = new SuperExpressionRightHandsideSubqueryPredicateBuilder(subqueryAlias, expr, this);
        return startBuilder(new GePredicateBuilder<T>(result, rightSuperExprSubqueryPredicateBuilderListener, leftExpression, subqueryInitFactory, expressionFactory, parameterManager, clause));
    }

    @Override
    public MultipleSubqueryInitiator<T> geSubqueries(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        return simpleMultipleBuilder(result, expr, new GePredicateBuilder<T>(result, null, leftExpression, subqueryInitFactory, expressionFactory, parameterManager, clause));
    }

    @Override
    public SubqueryBuilder<T> ge(FullQueryBuilder<?, ?> criteriaBuilder) {
        return startSubqueryBuilder(new GePredicateBuilder<T>(result, this, leftExpression, subqueryInitFactory, expressionFactory, parameterManager, clause), criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<T> ge(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        rightSuperExprSubqueryPredicateBuilderListener = new SuperExpressionRightHandsideSubqueryPredicateBuilder(subqueryAlias, expr, this);
        return startSubqueryBuilder(new GePredicateBuilder<T>(result, rightSuperExprSubqueryPredicateBuilderListener, leftExpression, subqueryInitFactory, expressionFactory, parameterManager, clause), criteriaBuilder);
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> lt() {
        return startBuilder(new LtPredicateBuilder<T>(result, this, leftExpression, subqueryInitFactory, expressionFactory, parameterManager, clause));
    }

    @Override
    public T lt(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return chain(new LtPredicate(leftExpression, parameterManager.addParameterExpression(value, clause, subqueryInitFactory.getQueryBuilder())));
    }

    @Override
    public T ltExpression(String expression) {
        return chain(new LtPredicate(leftExpression, expressionFactory.createSimpleExpression(expression, false)));
    }

    @Override
    public SubqueryInitiator<T> lt(String subqueryAlias, String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        rightSuperExprSubqueryPredicateBuilderListener = new SuperExpressionRightHandsideSubqueryPredicateBuilder(subqueryAlias, expr, this);
        return startBuilder(new LtPredicateBuilder<T>(result, rightSuperExprSubqueryPredicateBuilderListener, leftExpression, subqueryInitFactory, expressionFactory, parameterManager, clause));
    }

    @Override
    public MultipleSubqueryInitiator<T> ltSubqueries(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        return simpleMultipleBuilder(result, expr, new LtPredicateBuilder<T>(result, null, leftExpression, subqueryInitFactory, expressionFactory, parameterManager, clause));
    }

    @Override
    public SubqueryBuilder<T> lt(FullQueryBuilder<?, ?> criteriaBuilder) {
        return startSubqueryBuilder(new LtPredicateBuilder<T>(result, this, leftExpression, subqueryInitFactory, expressionFactory, parameterManager, clause), criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<T> lt(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        rightSuperExprSubqueryPredicateBuilderListener = new SuperExpressionRightHandsideSubqueryPredicateBuilder(subqueryAlias, expr, this);
        return startSubqueryBuilder(new LtPredicateBuilder<T>(result, rightSuperExprSubqueryPredicateBuilderListener, leftExpression, subqueryInitFactory, expressionFactory, parameterManager, clause), criteriaBuilder);
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> le() {
        return startBuilder(new LePredicateBuilder<T>(result, this, leftExpression, subqueryInitFactory, expressionFactory, parameterManager, clause));
    }

    @Override
    public T le(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return chain(new LePredicate(leftExpression, parameterManager.addParameterExpression(value, clause, subqueryInitFactory.getQueryBuilder())));
    }

    @Override
    public T leExpression(String expression) {
        return chain(new LePredicate(leftExpression, expressionFactory.createSimpleExpression(expression, false)));
    }

    @Override
    public SubqueryInitiator<T> le(String subqueryAlias, String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        rightSuperExprSubqueryPredicateBuilderListener = new SuperExpressionRightHandsideSubqueryPredicateBuilder(subqueryAlias, expr, this);
        return startBuilder(new LePredicateBuilder<T>(result, rightSuperExprSubqueryPredicateBuilderListener, leftExpression, subqueryInitFactory, expressionFactory, parameterManager, clause));
    }

    @Override
    public MultipleSubqueryInitiator<T> leSubqueries(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        return simpleMultipleBuilder(result, expr, new LePredicateBuilder<T>(result, null, leftExpression, subqueryInitFactory, expressionFactory, parameterManager, clause));
    }

    @Override
    public SubqueryBuilder<T> le(FullQueryBuilder<?, ?> criteriaBuilder) {
        return startSubqueryBuilder(new LePredicateBuilder<T>(result, this, leftExpression, subqueryInitFactory, expressionFactory, parameterManager, clause), criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<T> le(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        rightSuperExprSubqueryPredicateBuilderListener = new SuperExpressionRightHandsideSubqueryPredicateBuilder(subqueryAlias, expr, this);
        return startSubqueryBuilder(new LePredicateBuilder<T>(result, rightSuperExprSubqueryPredicateBuilderListener, leftExpression, subqueryInitFactory, expressionFactory, parameterManager, clause), criteriaBuilder);
    }

    @Override
    public T inExpressions(String... parameterOrLiteralExpressions) {
        if (parameterOrLiteralExpressions == null) {
            throw new NullPointerException("parameterOrLiteralExpressions");
        }
        if (parameterOrLiteralExpressions.length == 0) {
            throw new IllegalArgumentException("empty parameterOrLiteralExpressions");
        }
        
        return chain(new InPredicate(leftExpression, expressionFactory.createInItemExpressions(parameterOrLiteralExpressions)));
    }

    @Override
    public T in(Collection<?> values) {
        if (values == null) {
            throw new NullPointerException("values");
        }
        return chain(new InPredicate(leftExpression, parameterManager.addParameterExpression(values, clause, subqueryInitFactory.getQueryBuilder())));
    }

    @Override
    public T in(Object... values) {
        return in(Arrays.asList(values));
    }

    @Override
    public T notInExpressions(String... parameterOrLiteralExpressions) {
        if (parameterOrLiteralExpressions == null) {
            throw new NullPointerException("parameterOrLiteralExpressions");
        }
        if (parameterOrLiteralExpressions.length == 0) {
            throw new IllegalArgumentException("empty parameterOrLiteralExpressions");
        }

        return chain(new InPredicate(true, leftExpression, expressionFactory.createInItemExpressions(parameterOrLiteralExpressions)));
    }

    @Override
    public T notIn(Collection<?> values) {
        if (values == null) {
            throw new NullPointerException("values");
        }
        InPredicate inPredicate = new InPredicate(leftExpression, parameterManager.addParameterExpression(values, clause, subqueryInitFactory.getQueryBuilder()));
        inPredicate.setNegated(true);
        return chain(inPredicate);
    }

    @Override
    public T notIn(Object... values) {
        return notIn(Arrays.asList(values));
    }

    @Override
    public T isNull() {
        return chain(new IsNullPredicate(leftExpression));
    }

    @Override
    public T isNotNull() {
        return chain(new IsNullPredicate(leftExpression, true));
    }

    @Override
    public T isEmpty() {
        return chain(new IsEmptyPredicate(makeCollectionValued(leftExpression)));
    }

    @Override
    public T isNotEmpty() {
        return chain(new IsEmptyPredicate(makeCollectionValued(leftExpression), true));
    }

    @Override
    public T isMemberOf(String expression) {
        return chain(new MemberOfPredicate(leftExpression, makeCollectionValued(expressionFactory.createPathExpression(expression))));
    }

    @Override
    public T isNotMemberOf(String expression) {
        return chain(new MemberOfPredicate(leftExpression, makeCollectionValued(expressionFactory.createPathExpression(expression)), true));
    }

    @Override
    public LikeBuilder<T> like(boolean caseSensitive) {
        return startBuilder(new LikeBuilderImpl<T>(result, this, leftExpression, expressionFactory, parameterManager, subqueryInitFactory, clause, false, caseSensitive));
    }

    @Override
    public LikeBuilder<T> like() {
        return like(true);
    }

    @Override
    public LikeBuilder<T> notLike(boolean caseSensitive) {
        return startBuilder(new LikeBuilderImpl<T>(result, this, leftExpression, expressionFactory, parameterManager, subqueryInitFactory, clause, true, caseSensitive));
    }

    @Override
    public LikeBuilder<T> notLike() {
        return notLike(true);
    }

    @Override
    public SubqueryInitiator<T> in() {
        verifyBuilderEnded();
        this.predicate = new InPredicate(leftExpression);
        return subqueryInitFactory.createSubqueryInitiator(result, this, false, clause);
    }

    @Override
    public SubqueryInitiator<T> notIn() {
        verifyBuilderEnded();
        this.predicate = new InPredicate(true, leftExpression);
        return subqueryInitFactory.createSubqueryInitiator(result, this, false, clause);
    }

    @Override
    public SubqueryInitiator<T> in(String subqueryAlias, String expression) {
        return in(subqueryAlias, expression, false);
    }

    @Override
    public SubqueryInitiator<T> notIn(String subqueryAlias, String expression) {
        return in(subqueryAlias, expression, true);
    }

    @Override
    public SubqueryBuilder<T> in(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        return in(subqueryAlias, expression, false, criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<T> notIn(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        return in(subqueryAlias, expression, true, criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<T> in(FullQueryBuilder<?, ?> criteriaBuilder) {
        verifyBuilderEnded();
        this.predicate = new InPredicate(leftExpression);
        return subqueryInitFactory.createSubqueryBuilder(result, this, false, criteriaBuilder, clause);
    }

    @Override
    public SubqueryBuilder<T> notIn(FullQueryBuilder<?, ?> criteriaBuilder) {
        verifyBuilderEnded();
        this.predicate = new InPredicate(true, leftExpression);
        return subqueryInitFactory.createSubqueryBuilder(result, this, false, criteriaBuilder, clause);
    }

    private SubqueryInitiator<T> in(String subqueryAlias, String expression, boolean negated) {
        verifyBuilderEnded();

        this.predicate = new InPredicate(negated, leftExpression);
        Expression superExpression = expressionFactory.createArithmeticExpression(expression);
        rightSuperExprSubqueryBuilderListener = new SuperExpressionSubqueryBuilderListener<T>(subqueryAlias, superExpression) {

            @Override
            public void onBuilderEnded(SubqueryInternalBuilder<T> builder) {
                super.onBuilderEnded(builder);
                onSubqueryBuilderEnded(superExpression);
                listener.onBuilderEnded(RestrictionBuilderImpl.this);
            }

        };
        return subqueryInitFactory.createSubqueryInitiator(result, rightSuperExprSubqueryBuilderListener, false, clause);
    }

    private SubqueryBuilder<T> in(String subqueryAlias, String expression, boolean negated, FullQueryBuilder<?, ?> criteriaBuilder) {
        verifyBuilderEnded();

        this.predicate = new InPredicate(negated, leftExpression);
        Expression superExpression = expressionFactory.createArithmeticExpression(expression);
        rightSuperExprSubqueryBuilderListener = new SuperExpressionSubqueryBuilderListener<T>(subqueryAlias, superExpression) {

            @Override
            public void onBuilderEnded(SubqueryInternalBuilder<T> builder) {
                super.onBuilderEnded(builder);
                onSubqueryBuilderEnded(superExpression);
                listener.onBuilderEnded(RestrictionBuilderImpl.this);
            }

        };
        return subqueryInitFactory.createSubqueryBuilder(result, rightSuperExprSubqueryBuilderListener, false, criteriaBuilder, clause);
    }

    @Override
    public MultipleSubqueryInitiator<T> inSubqueries(String expression) {
        return inSubqueries(expression, false);
    }

    @Override
    public MultipleSubqueryInitiator<T> notInSubqueries(String expression) {
        return inSubqueries(expression, true);
    }
    
    private MultipleSubqueryInitiator<T> inSubqueries(String expression, boolean negated) {
        verifyBuilderEnded();

        Expression expr = expressionFactory.createSimpleExpression(expression, true);
        this.predicate = new InPredicate(negated, leftExpression);
        return new MultipleSubqueryInitiatorImpl<T>(result, expr, new ExpressionBuilderEndedListener() {
            
            @Override
            public void onBuilderEnded(ExpressionBuilder builder) {
                onSubqueryBuilderEnded(builder.getExpression());
                listener.onBuilderEnded(RestrictionBuilderImpl.this);
            }
            
        }, subqueryInitFactory, clause);
    }

    @Override
    public RestrictionBuilderExperimental<T> nonPortable() {
        return this;
    }

    @Override
    protected <X extends PredicateBuilder> X startBuilder(X builder) {
        betweenStartSubqueryBuilderListener.verifySubqueryBuilderEnded();
        return super.startBuilder(builder);
    }

    protected <X> SubqueryBuilder<X> startSubqueryBuilder(AbstractQuantifiablePredicateBuilder<X> builder, FullQueryBuilder<?, ?> criteriaBuilder) {
        betweenStartSubqueryBuilderListener.verifySubqueryBuilderEnded();
        return super.startBuilder(builder).one(criteriaBuilder);
    }

    @Override
    public void onBuilderEnded(PredicateBuilder builder) {
        super.onBuilderEnded(builder);
        predicate = builder.getPredicate();
        listener.onBuilderEnded(this);
    }

    @Override
    public void onBuilderEnded(SubqueryInternalBuilder<T> builder) {
        super.onBuilderEnded(builder);
        onSubqueryBuilderEnded(new SubqueryExpression(builder));
        listener.onBuilderEnded(this);
    }

    private void onSubqueryBuilderEnded(Expression rightHandsideExpression) {
        if (predicate instanceof InPredicate) {
            ((InPredicate) predicate).setRight(new ArrayList<Expression>(Arrays.asList(rightHandsideExpression)));
        } else {
            throw new IllegalStateException("SubqueryBuilder ended but predicate was not an IN predicate");
        }
    }

    @Override
    protected void verifyBuilderEnded() {
        super.verifyBuilderEnded();
        betweenStartSubqueryBuilderListener.verifySubqueryBuilderEnded();
        if (rightSuperExprSubqueryBuilderListener != null) {
            rightSuperExprSubqueryBuilderListener.verifySubqueryBuilderEnded();
        }
        if (leftSuperExprSubqueryPredicateBuilderListener != null) {
            leftSuperExprSubqueryPredicateBuilderListener.verifySubqueryBuilderEnded();
        }
    }

    private Expression makeCollectionValued(Expression expr) {
        if (expr instanceof PathExpression) {
            ((PathExpression) expr).setUsedInCollectionFunction(true);
        } else {
            throw new SyntaxErrorException("Function expects collection valued path and cannot be applied to predicate [" + expr + "]");
        }

        return expr;
    }

    /**
     * @author Christian Beikov
     * @since 1.0.0
     */
    private class SuperExpressionRightHandsideSubqueryPredicateBuilder implements PredicateBuilderEndedListener {

        private final PredicateBuilderEndedListener listener;
        private final String subqueryAlias;
        private Expression superExpression;

        public SuperExpressionRightHandsideSubqueryPredicateBuilder(String subqueryAlias, Expression superExpression, PredicateBuilderEndedListener listener) {
            this.listener = listener;
            this.subqueryAlias = subqueryAlias;
            this.superExpression = superExpression;
        }

        @Override
        public void onBuilderEnded(PredicateBuilder builder) {
            Predicate expression = builder.getPredicate();
            BinaryExpressionPredicate binaryPred = (BinaryExpressionPredicate) builder.getPredicate();
            SubqueryExpression subqueryExpr = (SubqueryExpression) binaryPred.getRight();
            superExpression = ExpressionUtils.replaceSubexpression(superExpression, subqueryAlias, subqueryExpr);
            binaryPred.setRight(superExpression);
            listener.onBuilderEnded(builder);
        }

    }
}
