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
package com.blazebit.persistence.impl;

import com.blazebit.persistence.QuantifiableBinaryPredicateBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.expression.SyntaxErrorException;
import com.blazebit.persistence.impl.predicate.BetweenPredicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilderEndedListener;
import com.blazebit.persistence.impl.predicate.EqPredicate;
import com.blazebit.persistence.impl.predicate.GePredicate;
import com.blazebit.persistence.impl.predicate.GtPredicate;
import com.blazebit.persistence.impl.predicate.InPredicate;
import com.blazebit.persistence.impl.predicate.IsEmptyPredicate;
import com.blazebit.persistence.impl.predicate.IsMemberOfPredicate;
import com.blazebit.persistence.impl.predicate.IsNullPredicate;
import com.blazebit.persistence.impl.predicate.LePredicate;
import com.blazebit.persistence.impl.predicate.LikePredicate;
import com.blazebit.persistence.impl.predicate.LtPredicate;
import com.blazebit.persistence.impl.predicate.NotPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;
import java.util.List;

/**
 *
 * @author cpbec
 */
public class RestrictionBuilderImpl<T> extends BuilderEndedListenerImpl implements RestrictionBuilder<T>, PredicateBuilder {

    private final T result;
    private final PredicateBuilderEndedListener listener;
    private final Expression leftExpression;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private Predicate predicate;
    private final ExpressionFactory expressionFactory;
    
    public RestrictionBuilderImpl(T result, PredicateBuilderEndedListener listener, Expression leftExpression, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory) {
        this.leftExpression = leftExpression;
        this.listener = listener;
        this.result = result;
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
    }

    private T chain(Predicate predicate) {
        verifyBuilderEnded();
        this.predicate = predicate;
        listener.onBuilderEnded(this);
        return result;
    }

    @Override
    public void onBuilderEnded(PredicateBuilder builder) {
        super.onBuilderEnded(builder);
        predicate = builder.getPredicate();
        listener.onBuilderEnded(this);
    }

    @Override
    public Predicate getPredicate() {
        return predicate;
    }

    @Override
    public T between(Object start, Object end) {
        if (start == null) {
            throw new NullPointerException("start");
        }
        if (end == null) {
            throw new NullPointerException("end");
        }
        return chain(new BetweenPredicate(leftExpression, new ParameterExpression(start), new ParameterExpression(end)));
    }

    @Override
    public T notBetween(Object start, Object end) {
        if (start == null) {
            throw new NullPointerException("start");
        }
        if (end == null) {
            throw new NullPointerException("end");
        }
        return chain(new NotPredicate(new BetweenPredicate(leftExpression, new ParameterExpression(start), new ParameterExpression(end))));
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> eq() {
        return startBuilder(new EqPredicate.EqPredicateBuilder<T>(result, this, leftExpression, false, subqueryInitFactory, expressionFactory));
    }

    @Override
    public T eq(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return chain(new EqPredicate(leftExpression, new ParameterExpression(value)));
    }

    @Override
    public T eqExpression(String expression) {
        return chain(new EqPredicate(leftExpression, expressionFactory.createSimpleExpression(expression)));
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> notEq() {
        return startBuilder(new EqPredicate.EqPredicateBuilder<T>(result, this, leftExpression, true, subqueryInitFactory, expressionFactory));
    }

    @Override
    public T notEq(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return chain(new NotPredicate(new EqPredicate(leftExpression, new ParameterExpression(value))));
    }

    @Override
    public T notEqExpression(String expression) {
        return chain(new NotPredicate(new EqPredicate(leftExpression, expressionFactory.createSimpleExpression(expression))));
    }

    @Override
    public QuantifiableBinaryPredicateBuilder<T> gt() {
        return startBuilder(new GtPredicate.GtPredicateBuilder<T>(result, this, leftExpression, subqueryInitFactory, expressionFactory));
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
    public QuantifiableBinaryPredicateBuilder<T> ge() {
        return startBuilder(new GePredicate.GePredicateBuilder<T>(result, this, leftExpression, subqueryInitFactory, expressionFactory));
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
    public QuantifiableBinaryPredicateBuilder<T> lt() {
        return startBuilder(new LtPredicate.LtPredicateBuilder<T>(result, this, leftExpression, subqueryInitFactory, expressionFactory));
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
    public QuantifiableBinaryPredicateBuilder<T> le() {
        return startBuilder(new LePredicate.LePredicateBuilder<T>(result, this, leftExpression, subqueryInitFactory, expressionFactory));
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
    public T in(List<?> values) {
        if (values == null) {
            throw new NullPointerException("values");
        }
        return chain(new InPredicate(leftExpression, new ParameterExpression(values)));
    }

    @Override
    public T notIn(List<?> values) {
        if (values == null) {
            throw new NullPointerException("values");
        }
        return chain(new NotPredicate(new InPredicate(leftExpression, new ParameterExpression(values))));
    }

    @Override
    public T isNull() {
        return chain(new IsNullPredicate(leftExpression));
    }

    @Override
    public T isNotNull() {
        return chain(new NotPredicate(new IsNullPredicate(leftExpression)));
    }
    
    @Override
    public T isEmpty() {
        
        return chain(new IsEmptyPredicate(makeCollectionValued(leftExpression)));
    }

    @Override
    public T isNotEmpty() {
        return chain(new NotPredicate(new IsEmptyPredicate(makeCollectionValued(leftExpression))));
    }

    @Override
    public T isMemberOf(String expression) {
        return chain(new IsMemberOfPredicate(leftExpression, makeCollectionValued(expressionFactory.createSimpleExpression(expression))));
    }

    @Override
    public T isNotMemberOf(String expression) {
        return chain(new NotPredicate(new IsMemberOfPredicate(leftExpression, makeCollectionValued(expressionFactory.createSimpleExpression(expression)))));
    }

    @Override
    public T like(String value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return chain(new LikePredicate(leftExpression, new ParameterExpression((Object) value), true, null));
    }

    @Override
    public T like(String value, boolean caseSensitive) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return chain(new LikePredicate(leftExpression, new ParameterExpression((Object) value), caseSensitive, null));
    }

    @Override
    public T like(String value, boolean caseSensitive, Character escapeCharacter) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return chain(new LikePredicate(leftExpression, new ParameterExpression((Object) value), caseSensitive, escapeCharacter));
    }

    @Override
    public T likeExpression(String expression) {
        return chain(new LikePredicate(leftExpression, expressionFactory.createSimpleExpression(expression), true, null));
    }

    @Override
    public T likeExpression(String expression, boolean caseSensitive) {
        return chain(new LikePredicate(leftExpression, expressionFactory.createSimpleExpression(expression), caseSensitive, null));
    }

    @Override
    public T likeExpression(String expression, boolean caseSensitive, Character escapeCharacter) {
        return chain(new LikePredicate(leftExpression, expressionFactory.createSimpleExpression(expression), caseSensitive, escapeCharacter));
    }

    @Override
    public T notLike(String value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return chain(new NotPredicate(new LikePredicate(leftExpression, new ParameterExpression((Object) value), true, null)));
    }

    @Override
    public T notLike(String value, boolean caseSensitive) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return chain(new NotPredicate(new LikePredicate(leftExpression, new ParameterExpression((Object) value), caseSensitive, null)));
    }

    @Override
    public T notLike(String value, boolean caseSensitive, Character escapeCharacter) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return chain(new NotPredicate(new LikePredicate(leftExpression, new ParameterExpression((Object) value), caseSensitive, escapeCharacter)));
    }

    @Override
    public T notLikeExpression(String expression) {
        return chain(new NotPredicate(new LikePredicate(leftExpression, expressionFactory.createSimpleExpression(expression), true, null)));
    }

    @Override
    public T notLikeExpression(String expression, boolean caseSensitive) {
        return chain(new NotPredicate(new LikePredicate(leftExpression, expressionFactory.createSimpleExpression(expression), caseSensitive, null)));
    }

    @Override
    public T notLikeExpression(String expression, boolean caseSensitive, Character escapeCharacter) {
        return chain(new NotPredicate(new LikePredicate(leftExpression, expressionFactory.createSimpleExpression(expression), caseSensitive, escapeCharacter)));
    }

    @Override
    public SubqueryInitiator<T> in() {
        verifyBuilderEnded();
        this.predicate = new InPredicate(leftExpression, null);
        return subqueryInitFactory.createSubqueryInitiator(result, this);
    }

    @Override
    public SubqueryInitiator<T> notIn() {
        verifyBuilderEnded();
        this.predicate = new NotPredicate(new InPredicate(leftExpression, null));
        return subqueryInitFactory.createSubqueryInitiator(result, this);
    }

    @Override
    public void onBuilderEnded(SubqueryBuilder<?> builder) {
        super.onBuilderEnded(builder);
        // set the finished subquery builder on the previously created predicate
        Predicate pred;
        if (predicate instanceof NotPredicate) {
            // unwrap not predicate
            pred = ((NotPredicate) predicate).getPredicate();
        } else {
            pred = predicate;
        }

        if(pred instanceof InPredicate){
            ((InPredicate)pred).setRight(new SubqueryExpression(builder));
        } else {
            throw new IllegalStateException("SubqueryBuilder ended but predicate was not a SubqueryPredicate");
        }
        listener.onBuilderEnded(this);
    }

    private Expression makeCollectionValued(Expression expr){
        if (leftExpression instanceof PathExpression) {
            ((PathExpression) leftExpression).setCollectionValued(true);
        } else {
            throw new SyntaxErrorException("Function expects collection valued path and cannot be applied to expression [" + leftExpression + "]");
        }
        return expr;
    }
}
