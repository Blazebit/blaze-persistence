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

package com.blazebit.persistence.impl.builder.object;

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.SelectObjectBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.BuilderChainingException;
import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.impl.MultipleSubqueryInitiatorImpl;
import com.blazebit.persistence.impl.SelectObjectBuilderEndedListener;
import com.blazebit.persistence.impl.SubqueryBuilderListenerImpl;
import com.blazebit.persistence.impl.SubqueryInitiatorFactory;
import com.blazebit.persistence.impl.SubqueryInternalBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListener;
import com.blazebit.persistence.impl.builder.expression.SuperExpressionSubqueryBuilderListener;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.SubqueryExpression;

import java.util.AbstractMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class SelectObjectBuilderImpl<T extends FullQueryBuilder<?, T>> extends SubqueryBuilderListenerImpl<SelectObjectBuilder<T>> implements SelectObjectBuilder<T>, ExpressionBuilderEndedListener {

    private final T result;
    // maps positions to expressions
    private final SortedMap<Integer, Map.Entry<Expression, String>> expressions = new TreeMap<Integer, Map.Entry<Expression, String>>();
    private final SelectObjectBuilderEndedListener listener;
    private final SubqueryInitiatorFactory subqueryInitFactory;
    private final ExpressionFactory expressionFactory;
    private String subqueryAlias;
    private Integer subqueryPosition;
    private SubqueryInitiator<?> subqueryStartMarker;
    private MultipleSubqueryInitiator<?> multipleSubqueryStartMarker;

    public SelectObjectBuilderImpl(T result, SelectObjectBuilderEndedListener listener, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory) {
        this.result = result;
        this.listener = listener;
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
    }

    @Override
    public SelectObjectBuilder<T> with(String expression) {
        return with(expression, null);
    }

    @Override
    public SelectObjectBuilder<T> with(String expression, String alias) {
        if (expressions.containsKey(expressions.size())) {
            throw new IllegalStateException("Argument for position " + expressions.size() + " already specified");
        }

        verifySubqueryBuilderEnded();
        Expression exp = expressionFactory.createSimpleExpression(expression, false);
        expressions.put(expressions.size(), new AbstractMap.SimpleEntry<Expression, String>(exp, alias));
        return this;
    }

    @Override
    public SelectObjectBuilder<T> with(int position, String expression) {
        return with(position, expression, null);
    }

    @Override
    public SelectObjectBuilder<T> with(int position, String expression, String alias) {
        if (expressions.containsKey(position)) {
            throw new IllegalStateException("Argument for position " + position + " already specified");
        }
        verifySubqueryBuilderEnded();
        Expression exp = expressionFactory.createSimpleExpression(expression, false);
        expressions.put(position, new AbstractMap.SimpleEntry<Expression, String>(exp, alias));
        return this;
    }

    @Override
    public T end() {
        listener.onBuilderEnded(expressions.values());
        return result;
    }

    @Override
    public void verifySubqueryBuilderEnded() {
        if (subqueryStartMarker != null) {
            throw new BuilderChainingException("A builder was not ended properly.");
        }
        if (multipleSubqueryStartMarker != null) {
            throw new BuilderChainingException("A builder was not ended properly.");
        }
        super.verifySubqueryBuilderEnded();
    }

    public <X> SubqueryInitiator<X> startSubqueryInitiator(SubqueryInitiator<X> subqueryInitiator) {
        this.subqueryStartMarker = subqueryInitiator;
        return subqueryInitiator;
    }

    @Override
    public SubqueryInitiator<SelectObjectBuilder<T>> withSubquery() {
        return withSubquery((String) null);
    }

    @Override
    public SubqueryInitiator<SelectObjectBuilder<T>> withSubquery(String alias) {
        verifySubqueryBuilderEnded();
        subqueryAlias = alias;
        return startSubqueryInitiator(subqueryInitFactory.createSubqueryInitiator((SelectObjectBuilder<T>) this, this, false, ClauseType.SELECT));
    }

    @Override
    public SubqueryInitiator<SelectObjectBuilder<T>> withSubquery(String subqueryAlias, String expression) {
        return withSubquery(subqueryAlias, expression, (String) null);
    }

    @Override
    public SubqueryInitiator<SelectObjectBuilder<T>> withSubquery(String subqueryAlias, String expression, String selectAlias) {
        verifySubqueryBuilderEnded();
        this.subqueryAlias = selectAlias;
        SubqueryBuilderListenerImpl<SelectObjectBuilder<T>> superExpressionSubqueryListener = new SuperExpressionSubqueryBuilderListener<SelectObjectBuilder<T>>(subqueryAlias, expressionFactory.createArithmeticExpression(expression));
        return startSubqueryInitiator(subqueryInitFactory.createSubqueryInitiator((SelectObjectBuilder<T>) this, superExpressionSubqueryListener, false, ClauseType.SELECT));
    }

    @Override
    public SubqueryInitiator<SelectObjectBuilder<T>> withSubquery(int position) {
        subqueryPosition = position;
        return withSubquery();
    }

    @Override
    public SubqueryInitiator<SelectObjectBuilder<T>> withSubquery(int position, String alias) {
        subqueryPosition = position;
        return withSubquery(alias);
    }

    @Override
    public SubqueryInitiator<SelectObjectBuilder<T>> withSubquery(int position, String subqueryAlias, String expression, String selectAlias) {
        subqueryPosition = position;
        return withSubquery(subqueryAlias, expression, selectAlias);
    }

    @Override
    public SubqueryInitiator<SelectObjectBuilder<T>> withSubquery(int position, String subqueryAlias, String expression) {
        subqueryPosition = position;
        return withSubquery(subqueryAlias, expression);
    }

    @Override
    public SubqueryBuilder<SelectObjectBuilder<T>> withSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        return withSubquery(null, criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<SelectObjectBuilder<T>> withSubquery(String alias, FullQueryBuilder<?, ?> criteriaBuilder) {
        verifySubqueryBuilderEnded();
        subqueryAlias = alias;
        return startSubqueryBuilder(subqueryInitFactory.createSubqueryBuilder(this, this, false, criteriaBuilder, ClauseType.SELECT));
    }

    @Override
    public SubqueryBuilder<SelectObjectBuilder<T>> withSubquery(String subqueryAlias, String expression, String selectAlias, FullQueryBuilder<?, ?> criteriaBuilder) {
        verifySubqueryBuilderEnded();
        this.subqueryAlias = selectAlias;
        SubqueryBuilderListenerImpl<SelectObjectBuilder<T>> superExpressionSubqueryListener = new SuperExpressionSubqueryBuilderListener<SelectObjectBuilder<T>>(subqueryAlias, expressionFactory.createArithmeticExpression(expression));
        return startSubqueryBuilder(subqueryInitFactory.createSubqueryBuilder(this, superExpressionSubqueryListener, false, criteriaBuilder, ClauseType.SELECT));
    }

    @Override
    public SubqueryBuilder<SelectObjectBuilder<T>> withSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        return withSubquery(subqueryAlias, expression, null, criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<SelectObjectBuilder<T>> withSubquery(int position, FullQueryBuilder<?, ?> criteriaBuilder) {
        subqueryPosition = position;
        return withSubquery(criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<SelectObjectBuilder<T>> withSubquery(int position, String alias, FullQueryBuilder<?, ?> criteriaBuilder) {
        subqueryPosition = position;
        return withSubquery(alias, criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<SelectObjectBuilder<T>> withSubquery(int position, String subqueryAlias, String expression, String selectAlias, FullQueryBuilder<?, ?> criteriaBuilder) {
        subqueryPosition = position;
        return withSubquery(subqueryAlias, expression, selectAlias, criteriaBuilder);
    }

    @Override
    public SubqueryBuilder<SelectObjectBuilder<T>> withSubquery(int position, String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        subqueryPosition = position;
        return withSubquery(subqueryAlias, expression, criteriaBuilder);
    }

    @Override
    public MultipleSubqueryInitiator<SelectObjectBuilder<T>> withSubqueries(String expression) {
        return withSubqueries(expression, null);
    }

    @Override
    public MultipleSubqueryInitiator<SelectObjectBuilder<T>> withSubqueries(String expression, String selectAlias) {
        this.subqueryAlias = selectAlias;
        return startMultipleSubqueryInitiator(expressionFactory.createArithmeticExpression(expression));
    }

    private MultipleSubqueryInitiator<SelectObjectBuilder<T>> startMultipleSubqueryInitiator(Expression expression) {
        verifySubqueryBuilderEnded();
        MultipleSubqueryInitiator<SelectObjectBuilder<T>> initiator = new MultipleSubqueryInitiatorImpl<SelectObjectBuilder<T>>(this, expression, this, subqueryInitFactory, ClauseType.SELECT);
        multipleSubqueryStartMarker = initiator;
        return initiator;
    }

    @Override
    public MultipleSubqueryInitiator<SelectObjectBuilder<T>> withSubqueries(int position, String expression, String selectAlias) {
        subqueryPosition = position;
        return withSubqueries(expression, selectAlias);
    }

    @Override
    public MultipleSubqueryInitiator<SelectObjectBuilder<T>> withSubqueries(int position, String expression) {
        subqueryPosition = position;
        return withSubqueries(expression);
    }

    @Override
    public void onBuilderEnded(SubqueryInternalBuilder<SelectObjectBuilder<T>> builder) {
        super.onBuilderEnded(builder);
        int position;
        if (subqueryPosition == null) {
            position = expressions.size();
        } else {
            position = subqueryPosition;
            subqueryPosition = null;
        }
        expressions.put(position, new AbstractMap.SimpleEntry<Expression, String>(new SubqueryExpression(builder), subqueryAlias));
    }

    @Override
    public void onBuilderEnded(ExpressionBuilder builder) {
        multipleSubqueryStartMarker = null;
        int position;
        if (subqueryPosition == null) {
            position = expressions.size();
        } else {
            position = subqueryPosition;
            subqueryPosition = null;
        }
        expressions.put(position, new AbstractMap.SimpleEntry<Expression, String>(builder.getExpression(), subqueryAlias));
    }

}
