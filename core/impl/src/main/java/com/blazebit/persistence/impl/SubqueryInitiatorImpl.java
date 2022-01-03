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

import com.blazebit.persistence.BaseFromQueryBuilder;
import com.blazebit.persistence.CommonQueryBuilder;
import com.blazebit.persistence.FullSelectCTECriteriaBuilder;
import com.blazebit.persistence.LeafOngoingFinalSetOperationSubqueryBuilder;
import com.blazebit.persistence.StartOngoingSetOperationSubqueryBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;
import com.blazebit.persistence.parser.expression.ExpressionFactory;

import javax.persistence.metamodel.EntityType;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.0.0
 */
public class SubqueryInitiatorImpl<X> implements SubqueryInitiator<X> {

    private final MainQuery mainQuery;
    private final AliasManager aliasManager;
    private final JoinManager parentJoinManager;
    private final ExpressionFactory expressionFactory;
    
    private final X result;
    private final SubqueryBuilderListener<X> listener;
    private final boolean inExists;
    private final QueryContext queryContext;

    public SubqueryInitiatorImpl(MainQuery mainQuery, QueryContext queryContext, AliasManager aliasManager, JoinManager parentJoinManager, X result, SubqueryBuilderListener<X> listener, boolean inExists) {
        this.mainQuery = mainQuery;
        this.aliasManager = aliasManager;
        this.parentJoinManager = parentJoinManager;
        this.expressionFactory = mainQuery.subqueryExpressionFactory;
        this.result = result;
        this.listener = listener;
        this.inExists = inExists;
        this.queryContext = queryContext;
    }

    @Override
    public CommonQueryBuilder<?> getParentQueryBuilder() {
        return (CommonQueryBuilder<?>) queryContext.getParent();
    }

    @Override
    public SubqueryBuilder<X> from(Class<?> clazz) {
        return from(clazz, null);
    }

    @Override
    public SubqueryBuilder<X> from(Class<?> clazz, String alias) {
        return from(clazz, alias, false);
    }

    public SubqueryBuilder<X> from(Class<?> clazz, String alias, boolean endResultAsJoinOnBuilder) {
        SubqueryBuilderImpl<X> subqueryBuilder = new SubqueryBuilderImpl<X>(mainQuery, queryContext, aliasManager, parentJoinManager, mainQuery.subqueryExpressionFactory, result, endResultAsJoinOnBuilder, listener);
        if (inExists) {
            subqueryBuilder.selectManager.setDefaultSelect(null, Arrays.asList(new SelectInfo(expressionFactory.createSimpleExpression("1"))), ExpressionCopyContext.EMPTY);
        }
        subqueryBuilder.from(clazz, alias);
        listener.onBuilderStarted(subqueryBuilder);
        return subqueryBuilder;
    }

    @Override
    public SubqueryBuilder<X> from(EntityType<?> entityType) {
        return from(entityType, null);
    }

    @Override
    public SubqueryBuilder<X> from(EntityType<?> entityType, String alias) {
        return from(entityType, alias, false);
    }

    public SubqueryBuilder<X> from(EntityType<?> entityType, String alias, boolean endResultAsJoinOnBuilder) {
        SubqueryBuilderImpl<X> subqueryBuilder = new SubqueryBuilderImpl<X>(mainQuery, queryContext, aliasManager, parentJoinManager, mainQuery.subqueryExpressionFactory, result, endResultAsJoinOnBuilder, listener);
        if (inExists) {
            subqueryBuilder.selectManager.setDefaultSelect(null, Arrays.asList(new SelectInfo(expressionFactory.createSimpleExpression("1"))), ExpressionCopyContext.EMPTY);
        }
        subqueryBuilder.from(entityType, alias);
        listener.onBuilderStarted(subqueryBuilder);
        return subqueryBuilder;
    }

    @Override
    public SubqueryBuilder<X> from(String correlationPath) {
        return from(correlationPath, null);
    }

    @Override
    public SubqueryBuilder<X> from(String correlationPath, String alias) {
        return from(correlationPath, alias, false);
    }

    public SubqueryBuilder<X> from(String correlationPath, String alias, boolean endResultAsJoinOnBuilder) {
        SubqueryBuilderImpl<X> subqueryBuilder = new SubqueryBuilderImpl<X>(mainQuery, queryContext, aliasManager, parentJoinManager, mainQuery.subqueryExpressionFactory, result, endResultAsJoinOnBuilder, listener);
        if (inExists) {
            subqueryBuilder.selectManager.setDefaultSelect(null, Arrays.asList(new SelectInfo(expressionFactory.createSimpleExpression("1"))), ExpressionCopyContext.EMPTY);
        }
        subqueryBuilder.from(correlationPath, alias);
        listener.onBuilderStarted(subqueryBuilder);
        return subqueryBuilder;
    }

    @Override
    public StartOngoingSetOperationSubqueryBuilder<X, LeafOngoingFinalSetOperationSubqueryBuilder<X>> startSet() {
        FinalSetOperationSubqueryBuilderImpl<X> parentFinalSetOperationBuilder = new FinalSetOperationSubqueryBuilderImpl<X>(mainQuery, queryContext, result, false, null, false, listener, null);
        OngoingFinalSetOperationSubqueryBuilderImpl<X> subFinalSetOperationBuilder = new OngoingFinalSetOperationSubqueryBuilderImpl<X>(mainQuery, queryContext, null, false, null, true, parentFinalSetOperationBuilder.getSubListener(), null);
        listener.onBuilderStarted(parentFinalSetOperationBuilder);
        
        LeafOngoingSetOperationSubqueryBuilderImpl<X> leafCb = new LeafOngoingSetOperationSubqueryBuilderImpl<X>(mainQuery, queryContext, aliasManager, parentJoinManager, expressionFactory, result, parentFinalSetOperationBuilder.getSubListener(), parentFinalSetOperationBuilder);
        StartOngoingSetOperationSubqueryBuilderImpl<X, LeafOngoingFinalSetOperationSubqueryBuilder<X>> cb = new StartOngoingSetOperationSubqueryBuilderImpl<X, LeafOngoingFinalSetOperationSubqueryBuilder<X>>(
                mainQuery, queryContext, aliasManager, parentJoinManager, expressionFactory, result, subFinalSetOperationBuilder.getSubListener(), subFinalSetOperationBuilder, leafCb
        );
        
        subFinalSetOperationBuilder.setOperationManager.setStartQueryBuilder(cb);
        parentFinalSetOperationBuilder.setOperationManager.setStartQueryBuilder(subFinalSetOperationBuilder);
        
        subFinalSetOperationBuilder.getSubListener().onBuilderStarted(cb);
        parentFinalSetOperationBuilder.getSubListener().onBuilderStarted(leafCb);
        return cb;
    }

    @Override
    public SubqueryBuilder<X> fromOld(Class<?> entityClass) {
        return fromOld(entityClass, null);
    }

    @Override
    public SubqueryBuilder<X> fromOld(Class<?> entityClass, String alias) {
        SubqueryBuilderImpl<X> subqueryBuilder = new SubqueryBuilderImpl<X>(mainQuery, queryContext, aliasManager, parentJoinManager, mainQuery.subqueryExpressionFactory, result, false, listener);
        if (inExists) {
            subqueryBuilder.selectManager.setDefaultSelect(null, Arrays.asList(new SelectInfo(expressionFactory.createSimpleExpression("1"))), ExpressionCopyContext.EMPTY);
        }
        subqueryBuilder.fromOld(entityClass, alias);
        listener.onBuilderStarted(subqueryBuilder);
        return subqueryBuilder;
    }

    @Override
    public SubqueryBuilder<X> fromNew(Class<?> entityClass) {
        return fromNew(entityClass, null);
    }

    @Override
    public SubqueryBuilder<X> fromNew(Class<?> entityClass, String alias) {
        SubqueryBuilderImpl<X> subqueryBuilder = new SubqueryBuilderImpl<X>(mainQuery, queryContext, aliasManager, parentJoinManager, mainQuery.subqueryExpressionFactory, result, false, listener);
        if (inExists) {
            subqueryBuilder.selectManager.setDefaultSelect(null, Arrays.asList(new SelectInfo(expressionFactory.createSimpleExpression("1"))), ExpressionCopyContext.EMPTY);
        }
        subqueryBuilder.fromNew(entityClass, alias);
        listener.onBuilderStarted(subqueryBuilder);
        return subqueryBuilder;
    }

    @Override
    public SubqueryBuilder<X> fromValues(Class<?> valueClass, String alias, int valueCount) {
        SubqueryBuilderImpl<X> subqueryBuilder = new SubqueryBuilderImpl<X>(mainQuery, queryContext, aliasManager, parentJoinManager, mainQuery.subqueryExpressionFactory, result, false, listener);
        if (inExists) {
            subqueryBuilder.selectManager.setDefaultSelect(null, Arrays.asList(new SelectInfo(expressionFactory.createSimpleExpression("1"))), ExpressionCopyContext.EMPTY);
        }
        subqueryBuilder.fromValues(valueClass, alias, valueCount);
        listener.onBuilderStarted(subqueryBuilder);
        return subqueryBuilder;
    }

    @Override
    public SubqueryBuilder<X> fromValues(Class<?> entityBaseClass, String attributeName, String alias, int valueCount) {
        SubqueryBuilderImpl<X> subqueryBuilder = new SubqueryBuilderImpl<X>(mainQuery, queryContext, aliasManager, parentJoinManager, mainQuery.subqueryExpressionFactory, result, false, listener);
        if (inExists) {
            subqueryBuilder.selectManager.setDefaultSelect(null, Arrays.asList(new SelectInfo(expressionFactory.createSimpleExpression("1"))), ExpressionCopyContext.EMPTY);
        }
        subqueryBuilder.fromValues(entityBaseClass, attributeName, alias, valueCount);
        listener.onBuilderStarted(subqueryBuilder);
        return subqueryBuilder;
    }

    @Override
    public SubqueryBuilder<X> fromIdentifiableValues(Class<?> valueClass, String alias, int valueCount) {
        SubqueryBuilderImpl<X> subqueryBuilder = new SubqueryBuilderImpl<X>(mainQuery, queryContext, aliasManager, parentJoinManager, mainQuery.subqueryExpressionFactory, result, false, listener);
        if (inExists) {
            subqueryBuilder.selectManager.setDefaultSelect(null, Arrays.asList(new SelectInfo(expressionFactory.createSimpleExpression("1"))), ExpressionCopyContext.EMPTY);
        }
        subqueryBuilder.fromIdentifiableValues(valueClass, alias, valueCount);
        listener.onBuilderStarted(subqueryBuilder);
        return subqueryBuilder;
    }

    @Override
    public SubqueryBuilder<X> fromIdentifiableValues(Class<?> valueClass, String identifierAttribute, String alias, int valueCount) {
        SubqueryBuilderImpl<X> subqueryBuilder = new SubqueryBuilderImpl<X>(mainQuery, queryContext, aliasManager, parentJoinManager, mainQuery.subqueryExpressionFactory, result, false, listener);
        if (inExists) {
            subqueryBuilder.selectManager.setDefaultSelect(null, Arrays.asList(new SelectInfo(expressionFactory.createSimpleExpression("1"))), ExpressionCopyContext.EMPTY);
        }
        subqueryBuilder.fromIdentifiableValues(valueClass, identifierAttribute, alias, valueCount);
        listener.onBuilderStarted(subqueryBuilder);
        return subqueryBuilder;
    }

    @Override
    public <T> SubqueryBuilder<X> fromValues(Class<T> valueClass, String alias, Collection<T> values) {
        SubqueryBuilder<X> builder = fromValues(valueClass, alias, values.size());
        builder.setParameter(alias, values);
        return builder;
    }

    @Override
    public SubqueryBuilder<X> fromValues(Class<?> entityBaseClass, String attributeName, String alias, Collection<?> values) {
        SubqueryBuilder<X> builder = fromValues(entityBaseClass, attributeName, alias, values.size());
        builder.setParameter(alias, values);
        return builder;
    }

    @Override
    public <T> SubqueryBuilder<X> fromIdentifiableValues(Class<T> valueClass, String alias, Collection<T> values) {
        SubqueryBuilder<X> builder = fromIdentifiableValues(valueClass, alias, values.size());
        builder.setParameter(alias, values);
        return builder;
    }

    @Override
    public <T> SubqueryBuilder<X> fromIdentifiableValues(Class<T> valueClass, String identifierAttribute, String alias, Collection<T> values) {
        SubqueryBuilder<X> builder = fromIdentifiableValues(valueClass, identifierAttribute, alias, values.size());
        builder.setParameter(alias, values);
        return builder;
    }

    @Override
    public FullSelectCTECriteriaBuilder<SubqueryBuilder<X>> fromSubquery(Class<?> cteClass) {
        SubqueryBuilderImpl<X> subqueryBuilder = new SubqueryBuilderImpl<X>(mainQuery, queryContext, aliasManager, parentJoinManager, mainQuery.subqueryExpressionFactory, result, false, listener);
        listener.onBuilderStarted(subqueryBuilder);
        return subqueryBuilder.fromSubquery(cteClass);
    }

    @Override
    public FullSelectCTECriteriaBuilder<SubqueryBuilder<X>> fromSubquery(Class<?> cteClass, String alias) {
        SubqueryBuilderImpl<X> subqueryBuilder = new SubqueryBuilderImpl<X>(mainQuery, queryContext, aliasManager, parentJoinManager, mainQuery.subqueryExpressionFactory, result, false, listener);
        listener.onBuilderStarted(subqueryBuilder);
        return subqueryBuilder.fromSubquery(cteClass, alias);
    }

    @Override
    public <Z extends BaseFromQueryBuilder<SubqueryBuilder<X>, ? extends Z>> Z fromEntitySubquery(Class<?> cteClass) {
        SubqueryBuilderImpl<X> subqueryBuilder = new SubqueryBuilderImpl<X>(mainQuery, queryContext, aliasManager, parentJoinManager, mainQuery.subqueryExpressionFactory, result, false, listener);
        listener.onBuilderStarted(subqueryBuilder);
        return (Z) subqueryBuilder.fromEntitySubquery(cteClass);
    }

    @Override
    public <Z extends BaseFromQueryBuilder<SubqueryBuilder<X>, ? extends Z>> Z fromEntitySubquery(Class<?> cteClass, String alias) {
        SubqueryBuilderImpl<X> subqueryBuilder = new SubqueryBuilderImpl<X>(mainQuery, queryContext, aliasManager, parentJoinManager, mainQuery.subqueryExpressionFactory, result, false, listener);
        listener.onBuilderStarted(subqueryBuilder);
        return (Z) subqueryBuilder.fromEntitySubquery(cteClass, alias);
    }

    @Override
    public <Z extends BaseFromQueryBuilder<SubqueryBuilder<X>, ? extends Z>> Z fromEntitySubquery(Class<?> cteClass, String alias, String subqueryAlias) {
        SubqueryBuilderImpl<X> subqueryBuilder = new SubqueryBuilderImpl<X>(mainQuery, queryContext, aliasManager, parentJoinManager, mainQuery.subqueryExpressionFactory, result, false, listener);
        listener.onBuilderStarted(subqueryBuilder);
        return (Z) subqueryBuilder.fromEntitySubquery(cteClass, alias, subqueryAlias);
    }
}
