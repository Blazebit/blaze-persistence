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

package com.blazebit.persistence.impl;

import com.blazebit.persistence.CaseWhenStarterBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SimpleCaseWhenStarterBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.WhereBuilder;
import com.blazebit.persistence.WhereOrBuilder;
import com.blazebit.persistence.WindowBuilder;
import com.blazebit.persistence.WindowFrameBetweenBuilder;
import com.blazebit.persistence.WindowFrameBuilder;
import com.blazebit.persistence.WindowFrameExclusionBuilder;
import com.blazebit.persistence.impl.builder.predicate.WhereOrBuilderImpl;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.OrderByItem;
import com.blazebit.persistence.parser.expression.WindowDefinition;
import com.blazebit.persistence.parser.expression.WindowFrameExclusionType;
import com.blazebit.persistence.parser.expression.WindowFrameMode;
import com.blazebit.persistence.parser.expression.WindowFramePositionType;
import com.blazebit.persistence.parser.predicate.Predicate;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class WindowBuilderImpl<T> extends PredicateManager<WindowBuilderImpl<T>> implements WindowBuilder<T>, WhereBuilder<WindowBuilderImpl<T>>, WindowFrameBuilder<T>, WindowFrameExclusionBuilder<T>, WindowFrameBetweenBuilder<T> {

    private final WindowManager<T> windowManager;
    private final T result;
    private final String name;

    private final List<Expression> partitionExpressions = new ArrayList<>();
    private final List<OrderByItem> orderByExpressions = new ArrayList<>();

    private String baseWindowName;
    private WindowFrameMode frameMode;
    private WindowFramePositionType frameStartType;
    private Expression frameStartExpression;
    private WindowFramePositionType frameEndType;
    private Expression frameEndExpression;
    private WindowFrameExclusionType frameExclusionType;

    public WindowBuilderImpl(ResolvingQueryGenerator queryGenerator, ParameterManager parameterManager, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory, WindowManager<T> windowManager, T result, String name) {
        super(queryGenerator, parameterManager, subqueryInitFactory, expressionFactory);
        this.windowManager = windowManager;
        this.result = result;
        this.name = name;
    }

    @Override
    public ClauseType getClauseType() {
        return ClauseType.WINDOW;
    }

    @Override
    protected String getClauseName() {
        return "WINDOW";
    }

    @Override
    public <X extends WhereBuilder<X> & WindowBuilder<X>> X filter() {
        return (X) this;
    }

    @Override
    public WindowBuilder<T> partitionBy(String... partitionExpressions) {
        for (String partitionExpression : partitionExpressions) {
            Expression expr = expressionFactory.createSimpleExpression(partitionExpression, false);
            parameterManager.collectParameterRegistrations(expr, ClauseType.WINDOW, subqueryInitFactory.getQueryBuilder());
            this.partitionExpressions.add(expr);
        }

        return this;
    }

    @Override
    public WindowBuilder<T> partitionBy(String partitionExpression) {
        Expression expr = expressionFactory.createSimpleExpression(partitionExpression, false);
        parameterManager.collectParameterRegistrations(expr, ClauseType.WINDOW, subqueryInitFactory.getQueryBuilder());
        partitionExpressions.add(expr);
        return this;
    }

    @Override
    public WindowFrameBuilder<T> rows() {
        this.frameMode = WindowFrameMode.ROWS;
        return this;
    }

    @Override
    public WindowFrameBuilder<T> range() {
        this.frameMode = WindowFrameMode.RANGE;
        return this;
    }

    @Override
    public WindowFrameBuilder<T> groups() {
        this.frameMode = WindowFrameMode.GROUPS;
        return this;
    }

    public WindowBuilder<T> orderBy(String expression, boolean ascending) {
        return orderBy(expression, ascending, false);
    }

    @Override
    public WindowBuilder<T> orderBy(String expression, boolean ascending, boolean nullFirst) {
        Expression expr = expressionFactory.createSimpleExpression(expression, false);
        parameterManager.collectParameterRegistrations(expr, ClauseType.WINDOW, subqueryInitFactory.getQueryBuilder());
        orderByExpressions.add(new OrderByItem(ascending, nullFirst, expr));
        return this;
    }

    @Override
    public WindowBuilder<T> orderByAsc(String expression) {
        return orderBy(expression, true, false);
    }

    @Override
    public WindowBuilder<T> orderByAsc(String expression, boolean nullFirst) {
        return orderBy(expression, true, nullFirst);
    }

    @Override
    public WindowBuilder<T> orderByDesc(String expression) {
        return orderBy(expression, false, false);
    }

    @Override
    public WindowBuilder<T> orderByDesc(String expression, boolean nullFirst) {
        return orderBy(expression, false, nullFirst);
    }

    @Override
    public WindowFrameBetweenBuilder<T> betweenUnboundedPreceding() {
        this.frameStartType = WindowFramePositionType.UNBOUNDED_PRECEDING;
        return this;
    }

    @Override
    public WindowFrameBetweenBuilder<T> betweenPreceding(String expression) {
        this.frameStartExpression = expressionFactory.createSimpleExpression(expression, false);
        parameterManager.collectParameterRegistrations(frameStartExpression, ClauseType.WINDOW, subqueryInitFactory.getQueryBuilder());
        this.frameStartType = WindowFramePositionType.BOUNDED_PRECEDING;
        return this;
    }

    @Override
    public WindowFrameBetweenBuilder<T> betweenFollowing(String expression) {
        this.frameStartExpression = expressionFactory.createSimpleExpression(expression, false);
        parameterManager.collectParameterRegistrations(frameStartExpression, ClauseType.WINDOW, subqueryInitFactory.getQueryBuilder());
        this.frameStartType = WindowFramePositionType.BOUNDED_FOLLOWING;
        return this;
    }

    @Override
    public WindowFrameBetweenBuilder<T> betweenCurrentRow() {
        this.frameStartType = WindowFramePositionType.CURRENT_ROW;
        return this;
    }

    @Override
    public WindowFrameExclusionBuilder<T> unboundedPreceding() {
        this.frameStartType = WindowFramePositionType.UNBOUNDED_PRECEDING;
        return this;
    }

    @Override
    public WindowFrameExclusionBuilder<T> preceding(String expression) {
        this.frameStartExpression = expressionFactory.createSimpleExpression(expression, false);
        parameterManager.collectParameterRegistrations(frameStartExpression, ClauseType.WINDOW, subqueryInitFactory.getQueryBuilder());
        this.frameStartType = WindowFramePositionType.BOUNDED_PRECEDING;
        return this;
    }

    @Override
    public WindowFrameExclusionBuilder<T> currentRow() {
        this.frameStartType = WindowFramePositionType.CURRENT_ROW;
        return this;
    }

    @Override
    public WindowFrameExclusionBuilder<T> andUnboundedFollowing() {
        this.frameEndType = WindowFramePositionType.UNBOUNDED_FOLLOWING;
        return this;
    }

    @Override
    public WindowFrameExclusionBuilder<T> andPreceding(String expression) {
        this.frameEndExpression = expressionFactory.createSimpleExpression(expression, false);
        parameterManager.collectParameterRegistrations(frameEndExpression, ClauseType.WINDOW, subqueryInitFactory.getQueryBuilder());
        this.frameEndType = WindowFramePositionType.BOUNDED_PRECEDING;
        return this;
    }

    @Override
    public WindowFrameExclusionBuilder<T> andFollowing(String expression) {
        this.frameEndExpression = expressionFactory.createSimpleExpression(expression, false);
        parameterManager.collectParameterRegistrations(frameEndExpression, ClauseType.WINDOW, subqueryInitFactory.getQueryBuilder());
        this.frameEndType = WindowFramePositionType.BOUNDED_FOLLOWING;
        return this;
    }

    @Override
    public WindowFrameExclusionBuilder<T> andCurrentRow() {
        this.frameEndType = WindowFramePositionType.CURRENT_ROW;
        return this;
    }

    @Override
    public T excludeNoOthers() {
        // The default
        return end();
    }

    @Override
    public T excludeCurrentRow() {
        this.frameExclusionType = WindowFrameExclusionType.EXCLUDE_CURRENT_ROW;
        return end();
    }

    @Override
    public T excludeGroup() {
        this.frameExclusionType = WindowFrameExclusionType.EXCLUDE_GROUP;
        return end();
    }

    @Override
    public T excludeTies() {
        this.frameExclusionType = WindowFrameExclusionType.EXCLUDE_TIES;
        return end();
    }

    @Override
    public T end() {
        WindowFrameMode frameMode = this.frameMode;
        WindowFramePositionType frameStartType = this.frameStartType;
        WindowFramePositionType frameEndType = this.frameEndType;

        // The default
        if (frameMode == WindowFrameMode.RANGE && frameEndExpression == null && frameEndType == WindowFramePositionType.CURRENT_ROW) {
            frameEndType = null;
        }

        // The default
        if (frameMode == WindowFrameMode.RANGE && frameEndType == null && frameStartExpression == null && frameStartType == WindowFramePositionType.UNBOUNDED_PRECEDING) {
            frameStartType = null;
        }

        // The default
        if (frameMode == WindowFrameMode.RANGE && frameStartType == null && frameEndType == null) {
            frameMode = null;
        }

        windowManager.onBuilderEnded(name, new WindowDefinition(
                baseWindowName,
                partitionExpressions,
                orderByExpressions,
                rootPredicate.getPredicate(),
                frameMode,
                frameStartType,
                frameStartExpression,
                frameEndType,
                frameEndExpression,
                frameExclusionType
        ));
        return result;
    }

    /*
     * Where methods
     */
    public RestrictionBuilder<WindowBuilderImpl<T>> where(String expression) {
        Expression expr = expressionFactory.createSimpleExpression(expression, false);
        return restrict(this, expr);
    }

    public CaseWhenStarterBuilder<RestrictionBuilder<WindowBuilderImpl<T>>> whereCase() {
        return restrictCase(this);
    }

    public SimpleCaseWhenStarterBuilder<RestrictionBuilder<WindowBuilderImpl<T>>> whereSimpleCase(String expression) {
        return restrictSimpleCase(this, expressionFactory.createSimpleExpression(expression, false));
    }

    public WhereOrBuilder<WindowBuilderImpl<T>> whereOr() {
        return rootPredicate.startBuilder(new WhereOrBuilderImpl<WindowBuilderImpl<T>>(this, rootPredicate, subqueryInitFactory, expressionFactory, parameterManager));
    }

    @SuppressWarnings("unchecked")
    public SubqueryInitiator<WindowBuilderImpl<T>> whereExists() {
        return restrictExists(this);
    }

    @SuppressWarnings("unchecked")
    public SubqueryInitiator<WindowBuilderImpl<T>> whereNotExists() {
        return restrictNotExists(this);
    }

    @SuppressWarnings("unchecked")
    public SubqueryBuilder<WindowBuilderImpl<T>> whereExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        return restrictExists(this, criteriaBuilder);
    }

    @SuppressWarnings("unchecked")
    public SubqueryBuilder<WindowBuilderImpl<T>> whereNotExists(FullQueryBuilder<?, ?> criteriaBuilder) {
        return restrictNotExists(this, criteriaBuilder);
    }

    public SubqueryInitiator<RestrictionBuilder<WindowBuilderImpl<T>>> whereSubquery() {
        return restrict(this);
    }

    public SubqueryInitiator<RestrictionBuilder<WindowBuilderImpl<T>>> whereSubquery(String subqueryAlias, String expression) {
        return restrict(this, subqueryAlias, expression);
    }

    public MultipleSubqueryInitiator<RestrictionBuilder<WindowBuilderImpl<T>>> whereSubqueries(String expression) {
        return restrictSubqueries(this, expression);
    }

    public SubqueryBuilder<RestrictionBuilder<WindowBuilderImpl<T>>> whereSubquery(FullQueryBuilder<?, ?> criteriaBuilder) {
        return restrict(this, criteriaBuilder);
    }

    public SubqueryBuilder<RestrictionBuilder<WindowBuilderImpl<T>>> whereSubquery(String subqueryAlias, String expression, FullQueryBuilder<?, ?> criteriaBuilder) {
        return restrict(this, subqueryAlias, expression, criteriaBuilder);
    }

    @SuppressWarnings("unchecked")
    public WindowBuilderImpl<T> whereExpression(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        restrictExpression(predicate);
        return this;
    }

    @SuppressWarnings("unchecked")
    public MultipleSubqueryInitiator<WindowBuilderImpl<T>> whereExpressionSubqueries(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, true);
        return restrictExpressionSubqueries(this, predicate);
    }

    @SuppressWarnings("unchecked")
    public WindowBuilderImpl<T> setWhereExpression(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, false);
        restrictSetExpression(predicate);
        return this;
    }

    @SuppressWarnings("unchecked")
    public MultipleSubqueryInitiator<WindowBuilderImpl<T>> setWhereExpressionSubqueries(String expression) {
        Predicate predicate = expressionFactory.createBooleanExpression(expression, true);
        return restrictSetExpressionSubqueries(this, predicate);
    }
}
