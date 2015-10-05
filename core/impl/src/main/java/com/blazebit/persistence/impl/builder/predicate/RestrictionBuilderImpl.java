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

import java.util.Arrays;
import java.util.List;

import com.blazebit.persistence.BetweenBuilder;
import com.blazebit.persistence.LikeBuilder;
import com.blazebit.persistence.QuantifiableBinaryPredicateBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.ExpressionUtils;
import com.blazebit.persistence.impl.PredicateAndSubqueryBuilderEndedListener;
import com.blazebit.persistence.impl.SubqueryBuilderImpl;
import com.blazebit.persistence.impl.SubqueryBuilderListener;
import com.blazebit.persistence.impl.SubqueryBuilderListenerImpl;
import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.impl.builder.expression.SuperExpressionSubqueryBuilderListener;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.expression.SyntaxErrorException;
import com.blazebit.persistence.impl.predicate.BinaryExpressionPredicate;
import com.blazebit.persistence.impl.predicate.EqPredicate;
import com.blazebit.persistence.impl.predicate.GePredicate;
import com.blazebit.persistence.impl.predicate.GtPredicate;
import com.blazebit.persistence.impl.predicate.InPredicate;
import com.blazebit.persistence.impl.predicate.IsEmptyPredicate;
import com.blazebit.persistence.impl.predicate.IsNullPredicate;
import com.blazebit.persistence.impl.predicate.LePredicate;
import com.blazebit.persistence.impl.predicate.LtPredicate;
import com.blazebit.persistence.impl.predicate.MemberOfPredicate;
import com.blazebit.persistence.impl.predicate.NotPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;
import com.blazebit.persistence.internal.RestrictionBuilderExperimental;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class RestrictionBuilderImpl<T> extends PredicateAndSubqueryBuilderEndedListener<T> implements RestrictionBuilderExperimental<T>, LeftHandsideSubqueryPredicateBuilder {

    private final T result;
    private final PredicateBuilderEndedListener listener;
    private Expression leftExpression;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private Predicate predicate;
    private final ExpressionFactory expressionFactory;
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final SubqueryBuilderListenerImpl<BetweenBuilder<T>> betweenStartSubqueryBuilderListener = new LeftHandsideSubqueryPredicateBuilderListener();
    private SubqueryBuilderListenerImpl<T> rightSuperExprSubqueryBuilderListener;
    private SubqueryBuilderListenerImpl<BetweenBuilder<T>> leftSuperExprSubqueryPredicateBuilderListener;
    private SuperExpressionRightHandsideSubqueryPredicateBuilder rightSuperExprSubqueryPredicateBuilderListener;

    public RestrictionBuilderImpl(T result, PredicateBuilderEndedListener listener, Expression leftExpression, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory) {
        this.leftExpression = leftExpression;
        this.listener = listener;
        this.result = result;
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
    }

    public RestrictionBuilderImpl(T result, PredicateBuilderEndedListener listener, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory) {
        this.listener = listener;
        this.result = result;
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
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
        return startBuilder(new BetweenBuilderImpl<T>(result, leftExpression, new ParameterExpression(start), expressionFactory, this, subqueryInitFactory));
    }

    @Override
    public BetweenBuilder<T> betweenExpression(String start) {
        return startBuilder(new BetweenBuilderImpl<T>(result, leftExpression, expressionFactory.createArithmeticExpression(start), expressionFactory, this, subqueryInitFactory));
    }

    @Override
    public SubqueryInitiator<BetweenBuilder<T>> betweenSubquery() {
        BetweenBuilder<T> betweenBuilder = startBuilder(new BetweenBuilderImpl<T>(result, leftExpression, null, expressionFactory, this, subqueryInitFactory));
        return subqueryInitFactory.createSubqueryInitiator(betweenBuilder, (SubqueryBuilderListener<BetweenBuilder<T>>) betweenStartSubqueryBuilderListener);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public SubqueryInitiator<BetweenBuilder<T>> betweenSubquery(String subqueryAlias, String expression) {
        leftSuperExprSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression));
        BetweenBuilder<T> betweenBuilder = startBuilder(new BetweenBuilderImpl<T>(result, leftExpression, null, expressionFactory, this, subqueryInitFactory));
        return subqueryInitFactory.createSubqueryInitiator(betweenBuilder, (SubqueryBuilderListener<BetweenBuilder<T>>) leftSuperExprSubqueryPredicateBuilderListener);
    }

    @Override
    public BetweenBuilder<T> notBetween(Object start) {
        if (start == null) {
            throw new NullPointerException("start");
        }
        return startBuilder(new BetweenBuilderImpl<T>(result, leftExpression, new ParameterExpression(start), expressionFactory, this, subqueryInitFactory, true));
    }

    @Override
    public BetweenBuilder<T> notBetweenExpression(String start) {
        return startBuilder(new BetweenBuilderImpl<T>(result, leftExpression, expressionFactory.createArithmeticExpression(start), expressionFactory, this, subqueryInitFactory, true));
    }

    @Override
    public SubqueryInitiator<BetweenBuilder<T>> notBetweenSubquery() {
        BetweenBuilder<T> betweenBuilder = startBuilder(new BetweenBuilderImpl<T>(result, leftExpression, null, expressionFactory, this, subqueryInitFactory, true));
        return subqueryInitFactory.createSubqueryInitiator(betweenBuilder, (SubqueryBuilderListener<BetweenBuilder<T>>) betweenStartSubqueryBuilderListener);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public SubqueryInitiator<BetweenBuilder<T>> notBetweenSubquery(String subqueryAlias, String expression) {
        leftSuperExprSubqueryPredicateBuilderListener = new SuperExpressionLeftHandsideSubqueryPredicateBuilder(subqueryAlias, expressionFactory.createSimpleExpression(expression));
        BetweenBuilder<T> betweenBuilder = startBuilder(new BetweenBuilderImpl<T>(result, leftExpression, null, expressionFactory, this, subqueryInitFactory, true));
        return subqueryInitFactory.createSubqueryInitiator(betweenBuilder, (SubqueryBuilderListener<BetweenBuilder<T>>) leftSuperExprSubqueryPredicateBuilderListener);
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> eq() {
        return startBuilder(new EqPredicateBuilder<T>(result, this, leftExpression, false, subqueryInitFactory, expressionFactory));
    }

    @Override
    public T eq(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return chain(new EqPredicate(leftExpression, new ParameterExpression(value)));
    }

    @Override
    public SubqueryInitiator<T> eq(String subqueryAlias, String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression);
        rightSuperExprSubqueryPredicateBuilderListener = new SuperExpressionRightHandsideSubqueryPredicateBuilder(subqueryAlias, expr, this);
        return startBuilder(new EqPredicateBuilder<T>(result, rightSuperExprSubqueryPredicateBuilderListener, leftExpression, false, subqueryInitFactory, expressionFactory));
    }

    @Override
    public T eqExpression(String expression) {
        return chain(new EqPredicate(leftExpression, expressionFactory.createSimpleExpression(expression)));
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> notEq() {
        return startBuilder(new EqPredicateBuilder<T>(result, this, leftExpression, true, subqueryInitFactory, expressionFactory));
    }

    @Override
    public T notEq(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return chain(new EqPredicate(leftExpression, new ParameterExpression(value), true));
    }

    @Override
    public T notEqExpression(String expression) {
        return chain(new EqPredicate(leftExpression, expressionFactory.createSimpleExpression(expression), true));
    }

    @Override
    public SubqueryInitiator<T> notEq(String subqueryAlias, String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression);
        rightSuperExprSubqueryPredicateBuilderListener = new SuperExpressionRightHandsideSubqueryPredicateBuilder(subqueryAlias, expr, this);
        return startBuilder(new EqPredicateBuilder<T>(result, rightSuperExprSubqueryPredicateBuilderListener, leftExpression, true, subqueryInitFactory, expressionFactory));
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> gt() {
        return startBuilder(new GtPredicateBuilder<T>(result, this, leftExpression, subqueryInitFactory, expressionFactory));
    }

    @Override
    public T gt(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return chain(new GtPredicate(leftExpression, new ParameterExpression(value)));
    }

    @Override
    public T gtExpression(String expression) {
        return chain(new GtPredicate(leftExpression, expressionFactory.createSimpleExpression(expression)));
    }

    @Override
    public SubqueryInitiator<T> gt(String subqueryAlias, String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression);
        rightSuperExprSubqueryPredicateBuilderListener = new SuperExpressionRightHandsideSubqueryPredicateBuilder(subqueryAlias, expr, this);
        return startBuilder(new GtPredicateBuilder<T>(result, rightSuperExprSubqueryPredicateBuilderListener, leftExpression, subqueryInitFactory, expressionFactory));
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> ge() {
        return startBuilder(new GePredicateBuilder<T>(result, this, leftExpression, subqueryInitFactory, expressionFactory));
    }

    @Override
    public T ge(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return chain(new GePredicate(leftExpression, new ParameterExpression(value)));
    }

    @Override
    public T geExpression(String expression) {
        return chain(new GePredicate(leftExpression, expressionFactory.createSimpleExpression(expression)));
    }

    @Override
    public SubqueryInitiator<T> ge(String subqueryAlias, String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression);
        rightSuperExprSubqueryPredicateBuilderListener = new SuperExpressionRightHandsideSubqueryPredicateBuilder(subqueryAlias, expr, this);
        return startBuilder(new GePredicateBuilder<T>(result, rightSuperExprSubqueryPredicateBuilderListener, leftExpression, subqueryInitFactory, expressionFactory));
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> lt() {
        return startBuilder(new LtPredicateBuilder<T>(result, this, leftExpression, subqueryInitFactory, expressionFactory));
    }

    @Override
    public T lt(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return chain(new LtPredicate(leftExpression, new ParameterExpression(value)));
    }

    @Override
    public T ltExpression(String expression) {
        return chain(new LtPredicate(leftExpression, expressionFactory.createSimpleExpression(expression)));
    }

    @Override
    public SubqueryInitiator<T> lt(String subqueryAlias, String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression);
        rightSuperExprSubqueryPredicateBuilderListener = new SuperExpressionRightHandsideSubqueryPredicateBuilder(subqueryAlias, expr, this);
        return startBuilder(new LtPredicateBuilder<T>(result, rightSuperExprSubqueryPredicateBuilderListener, leftExpression, subqueryInitFactory, expressionFactory));
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> le() {
        return startBuilder(new LePredicateBuilder<T>(result, this, leftExpression, subqueryInitFactory, expressionFactory));
    }

    @Override
    public T le(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return chain(new LePredicate(leftExpression, new ParameterExpression(value)));
    }

    @Override
    public T leExpression(String expression) {
        return chain(new LePredicate(leftExpression, expressionFactory.createSimpleExpression(expression)));
    }

    @Override
    public SubqueryInitiator<T> le(String subqueryAlias, String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression);
        rightSuperExprSubqueryPredicateBuilderListener = new SuperExpressionRightHandsideSubqueryPredicateBuilder(subqueryAlias, expr, this);
        return startBuilder(new LePredicateBuilder<T>(result, rightSuperExprSubqueryPredicateBuilderListener, leftExpression, subqueryInitFactory, expressionFactory));
    }

    @Override
    public T inExpressions(String... parameterOrLiteralExpressions) {
        if (parameterOrLiteralExpressions == null) {
            throw new NullPointerException("parameterOrLiteralExpressions");
        }
        if (parameterOrLiteralExpressions.length == 0) {
            throw new IllegalArgumentException("empty parameterOrLiteralExpressions");
        }
        
        return chain(new InPredicate(leftExpression, expressionFactory.createInPredicateExpression(parameterOrLiteralExpressions)));
    }

    @Override
    public T in(List<?> values) {
        if (values == null) {
            throw new NullPointerException("values");
        }
        return chain(new InPredicate(leftExpression, new ParameterExpression(values)));
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
        
        return chain(new InPredicate(leftExpression, expressionFactory.createInPredicateExpression(parameterOrLiteralExpressions), true));
    }

    @Override
    public T notIn(List<?> values) {
        if (values == null) {
            throw new NullPointerException("values");
        }
        return chain(new InPredicate(leftExpression, new ParameterExpression(values), true));
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
        return chain(new MemberOfPredicate(leftExpression, makeCollectionValued(expressionFactory.createSimpleExpression(expression))));
    }

    @Override
    public T isNotMemberOf(String expression) {
        return chain(new MemberOfPredicate(leftExpression, makeCollectionValued(expressionFactory.createSimpleExpression(expression)), true));
    }

    @Override
    public LikeBuilder<T> like(boolean caseSensitive) {
        return startBuilder(new LikeBuilderImpl<T>(result, this, leftExpression, expressionFactory, subqueryInitFactory, false, caseSensitive));
    }

    @Override
    public LikeBuilder<T> like() {
        return like(true);
    }

    @Override
    public LikeBuilder<T> notLike(boolean caseSensitive) {
        return startBuilder(new LikeBuilderImpl<T>(result, this, leftExpression, expressionFactory, subqueryInitFactory, true, caseSensitive));
    }

    @Override
    public LikeBuilder<T> notLike() {
        return notLike(true);
    }

    @Override
    public SubqueryInitiator<T> in() {
        verifyBuilderEnded();
        this.predicate = new InPredicate(leftExpression, null);
        return subqueryInitFactory.createSubqueryInitiator(result, this);
    }

    @Override
    public SubqueryInitiator<T> in(String subqueryAlias, String expression) {
        verifyBuilderEnded();

        this.predicate = new InPredicate(leftExpression, null);
        rightSuperExprSubqueryBuilderListener = new SuperExpressionSubqueryBuilderListener<T>(subqueryAlias, expressionFactory.createArithmeticExpression(expression)) {

            @Override
            public void onBuilderEnded(SubqueryBuilderImpl<T> builder) {
                super.onBuilderEnded(builder);
                onSubqueryBuilderEnded(superExpression);
                listener.onBuilderEnded(RestrictionBuilderImpl.this);
            }

        };
        return subqueryInitFactory.createSubqueryInitiator(result, rightSuperExprSubqueryBuilderListener);
    }

    @Override
    public SubqueryInitiator<T> notIn(String subqueryAlias, String expression) {
        verifyBuilderEnded();

        this.predicate = new InPredicate(leftExpression, null, true);
        rightSuperExprSubqueryBuilderListener = new SuperExpressionSubqueryBuilderListener<T>(subqueryAlias, expressionFactory.createArithmeticExpression(expression)) {

            @Override
            public void onBuilderEnded(SubqueryBuilderImpl<T> builder) {
                super.onBuilderEnded(builder);
                onSubqueryBuilderEnded(superExpression);
                listener.onBuilderEnded(RestrictionBuilderImpl.this);
            }

        };
        return subqueryInitFactory.createSubqueryInitiator(result, rightSuperExprSubqueryBuilderListener);
    }

    @Override
    public SubqueryInitiator<T> notIn() {
        verifyBuilderEnded();
        this.predicate = new InPredicate(leftExpression, null, true);
        return subqueryInitFactory.createSubqueryInitiator(result, this);
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

    @Override
    public void onBuilderEnded(PredicateBuilder builder) {
        super.onBuilderEnded(builder);
        predicate = (Predicate) builder.getPredicate();
        listener.onBuilderEnded(this);
    }

    @Override
    public void onBuilderEnded(SubqueryBuilderImpl<T> builder) {
        super.onBuilderEnded(builder);
        onSubqueryBuilderEnded(new SubqueryExpression(builder));
        listener.onBuilderEnded(this);
    }

    private void onSubqueryBuilderEnded(Expression rightHandsideExpression) {
        if (predicate instanceof InPredicate) {
            ((InPredicate) predicate).setRight(rightHandsideExpression);
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
            throw new SyntaxErrorException("Function expects collection valued path and cannot be applied to expression [" + expr + "]");
        }

        return expr;
    }

    private class SuperExpressionRightHandsideSubqueryPredicateBuilder implements PredicateBuilderEndedListener {

        private final PredicateBuilderEndedListener listener;
        private final String subqueryAlias;
        private final Expression superExpression;

        public SuperExpressionRightHandsideSubqueryPredicateBuilder(String subqueryAlias, Expression superExpression, PredicateBuilderEndedListener listener) {
            this.listener = listener;
            this.subqueryAlias = subqueryAlias;
            this.superExpression = superExpression;
        }

        @Override
        public void onBuilderEnded(PredicateBuilder builder) {
            Predicate pred = builder.getPredicate();
            BinaryExpressionPredicate binaryPred;

            if (pred instanceof NotPredicate) {
                binaryPred = (BinaryExpressionPredicate) ((NotPredicate) pred).getPredicate();
            } else {
                binaryPred = (BinaryExpressionPredicate) builder.getPredicate();
            }

            SubqueryExpression subqueryExpr = (SubqueryExpression) binaryPred.getRight();
            ExpressionUtils.replaceSubexpression(superExpression, subqueryAlias, subqueryExpr);
            binaryPred.setRight(superExpression);
            listener.onBuilderEnded(builder);
        }

    }
}
