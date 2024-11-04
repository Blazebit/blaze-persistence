/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.BaseOngoingSetOperationBuilder;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.JpqlFunctionProcessor;
import com.blazebit.persistence.spi.SetOperationType;

import jakarta.persistence.Tuple;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public abstract class BaseSubqueryBuilderImpl<T, X, Y extends BaseOngoingSetOperationBuilder<?, ?, ?>, Z extends BaseOngoingSetOperationBuilder<?, ?, ?>> extends AbstractCommonQueryBuilder<Tuple, X, Y, Z, BaseFinalSetOperationSubqueryBuilderImpl<T, ?>> implements SubqueryInternalBuilder<T> {

    protected final T result;
    protected final boolean endResultAsJoinOnBuilder;
    protected final SubqueryBuilderListener<T> listener;
    protected final SubqueryBuilderListenerImpl<T> subListener;

    public BaseSubqueryBuilderImpl(MainQuery mainQuery, QueryContext queryContext, AliasManager aliasManager, JoinManager parentJoinManager, ExpressionFactory expressionFactory, T result, boolean endResultAsJoinOnBuilder, SubqueryBuilderListener<T> listener, BaseFinalSetOperationSubqueryBuilderImpl<T, ?> finalSetOperationBuilder) {
        super(mainQuery, queryContext, false, DbmsStatementType.SELECT, Tuple.class, null, aliasManager, parentJoinManager, expressionFactory, finalSetOperationBuilder, true);
        this.result = result;
        this.endResultAsJoinOnBuilder = endResultAsJoinOnBuilder;
        this.listener = listener;
        this.subListener = new SubqueryBuilderListenerImpl<T>();
    }

    public BaseSubqueryBuilderImpl(BaseSubqueryBuilderImpl<T, X, Y, Z> builder, MainQuery mainQuery, QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        super(builder, mainQuery, queryContext, joinManagerMapping, copyContext);
        this.result = null;
        this.endResultAsJoinOnBuilder = false;
        this.listener = null;
        this.subListener = null;
    }

    public SubqueryBuilderListenerImpl<T> getSubListener() {
        return subListener;
    }

    @Override
    public Map<Integer, JpqlFunctionProcessor<?>> getJpqlFunctionProcessors() {
        return selectManager.getJpqlFunctionProcessors();
    }

    @Override
    public List<Expression> getSelectExpressions() {
        List<Expression> selectExpressions = new ArrayList<Expression>(selectManager.getSelectInfos().size());

        for (SelectInfo info : selectManager.getSelectInfos()) {
            selectExpressions.add(info.getExpression());
        }

        return selectExpressions;
    }

    @Override
    public Set<Expression> getCorrelatedExpressions(AliasManager aliasManager) {
        prepareAndCheck(null);
        CorrelatedExpressionGatheringVisitor visitor = new CorrelatedExpressionGatheringVisitor(aliasManager);
        applyVisitor(visitor);
        Set<Expression> expressions = visitor.getExpressions();
        joinManager.collectCorrelatedRootExpressions(aliasManager, expressions);
        return expressions;
    }

    public T getResult() {
        return result;
    }

    public boolean isEndResultAsJoinOnBuilder() {
        return endResultAsJoinOnBuilder;
    }

    protected BaseFinalSetOperationSubqueryBuilderImpl<T, ?> createFinalSetOperationBuilder(SetOperationType operator, boolean nested, boolean isSubquery) {
        SubqueryBuilderImpl<?> newInitiator = finalSetOperationBuilder == null ? null : finalSetOperationBuilder.getInitiator();
        return createFinalSetOperationBuilder(operator, nested, isSubquery, newInitiator);
    }
    
    protected BaseFinalSetOperationSubqueryBuilderImpl<T, ?> createFinalSetOperationBuilder(SetOperationType operator, boolean nested, boolean isSubquery, SubqueryBuilderImpl<?> newInitiator) {
        // TODO: this should never be null, handle it!
        T newResult;
        boolean newEndResultAsJoinOnBuilder;
        SubqueryBuilderListener<T> newListener;

        if (finalSetOperationBuilder == null) {
            newResult = result;
            newEndResultAsJoinOnBuilder = endResultAsJoinOnBuilder;
            newListener = listener;
        } else {
            newResult = finalSetOperationBuilder.getResult();
            newListener = finalSetOperationBuilder.getSubListener();
            newEndResultAsJoinOnBuilder = finalSetOperationBuilder.isEndResultAsJoinOnBuilder();
        }
        
        if (isSubquery) {
            return new OngoingFinalSetOperationSubqueryBuilderImpl<T>(mainQuery, queryContext, newResult, newEndResultAsJoinOnBuilder, operator, nested, newListener, newInitiator);
        } else {
            return new FinalSetOperationSubqueryBuilderImpl<T>(mainQuery, queryContext, newResult, newEndResultAsJoinOnBuilder, operator, nested, newListener, newInitiator);
        }
    }

    @SuppressWarnings("unchecked")
    protected LeafOngoingSetOperationSubqueryBuilderImpl<T> createLeaf(BaseFinalSetOperationSubqueryBuilderImpl<T, ?> finalSetOperationBuilder) {
        SubqueryBuilderListener<T> newListener = finalSetOperationBuilder.getSubListener();
        LeafOngoingSetOperationSubqueryBuilderImpl<T> next = new LeafOngoingSetOperationSubqueryBuilderImpl<T>(mainQuery, queryContext, aliasManager.getParent(), joinManager.getParent(), expressionFactory, result, newListener, (FinalSetOperationSubqueryBuilderImpl<T>) finalSetOperationBuilder);
        newListener.onBuilderStarted(next);
        return next;
    }

    @SuppressWarnings("unchecked")
    protected <W> StartOngoingSetOperationSubqueryBuilderImpl<T, W> createStartOngoing(BaseFinalSetOperationSubqueryBuilderImpl<T, ?> finalSetOperationBuilder, W endSetResult) {
        // TODO: This is such an ugly hack, but I don't know how else to fix this generics issue for now
        finalSetOperationBuilder.setEndSetResult((T) endSetResult);
        
        SubqueryBuilderListener<T> newListener = finalSetOperationBuilder.getSubListener();
        StartOngoingSetOperationSubqueryBuilderImpl<T, W> next = new StartOngoingSetOperationSubqueryBuilderImpl<T, W>(mainQuery, queryContext, aliasManager.getParent(), joinManager.getParent(), expressionFactory, result, newListener, (OngoingFinalSetOperationSubqueryBuilderImpl<T>) finalSetOperationBuilder, endSetResult);
        newListener.onBuilderStarted(next);
        return next;
    }

    @SuppressWarnings("unchecked")
    protected <W> OngoingSetOperationSubqueryBuilderImpl<T, W> createOngoing(BaseFinalSetOperationSubqueryBuilderImpl<T, ?> finalSetOperationBuilder, W endSetResult) {
        // TODO: This is such an ugly hack, but I don't know how else to fix this generics issue for now
        finalSetOperationBuilder.setEndSetResult((T) endSetResult);

        SubqueryBuilderListener<T> newListener = finalSetOperationBuilder.getSubListener();
        OngoingSetOperationSubqueryBuilderImpl<T, W> next = new OngoingSetOperationSubqueryBuilderImpl<T, W>(mainQuery, queryContext, aliasManager.getParent(), joinManager.getParent(), expressionFactory, result, newListener, (OngoingFinalSetOperationSubqueryBuilderImpl<T>) finalSetOperationBuilder, endSetResult);
        newListener.onBuilderStarted(next);
        return next;
    }
}
