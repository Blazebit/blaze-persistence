/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;
import com.blazebit.persistence.parser.expression.InplaceModificationResultVisitorAdapter;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.parser.predicate.ExistsPredicate;

import java.util.Collections;
import java.util.IdentityHashMap;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class SubqueryInitiatorFactory {

    private final MainQuery mainQuery;
    private final AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder;
    private final AliasManager aliasManager;
    private final JoinManager parentJoinManager;
    private final SubqueryReattachingTransformationVisitor subqueryTransformationVisitor;

    public SubqueryInitiatorFactory(MainQuery mainQuery, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder, AliasManager aliasManager, JoinManager parentJoinManager) {
        this.mainQuery = mainQuery;
        this.queryBuilder = queryBuilder;
        this.aliasManager = aliasManager;
        this.parentJoinManager = parentJoinManager;
        this.subqueryTransformationVisitor = new SubqueryReattachingTransformationVisitor();
    }

    public AbstractCommonQueryBuilder<?, ?, ?, ?, ?> getQueryBuilder() {
        return queryBuilder;
    }

    public <T> SubqueryInitiatorImpl<T> createSubqueryInitiator(T result, SubqueryBuilderListener<T> listener, boolean inExists, ClauseType clause) {
        return new SubqueryInitiatorImpl<T>(mainQuery, new QueryContext(queryBuilder, clause), aliasManager, parentJoinManager, result, listener, inExists);
    }

    public <T> SubqueryBuilderImpl<T> createSubqueryBuilder(T result, SubqueryBuilderListener<T> listener, boolean inExists, FullQueryBuilder<?, ?> criteriaBuilder, ClauseType clause) {
        // TODO: paginated criteria builder?
        return createSubqueryBuilder(result, listener, inExists, (AbstractCommonQueryBuilder<?, ?, ?, ?, ?>) criteriaBuilder, clause);
    }

    public <T> SubqueryBuilderImpl<T> createSubqueryBuilder(T result, SubqueryBuilderListener<T> listener, boolean inExists, SubqueryBuilderImpl<?> subqueryBuilder, ClauseType clause) {
        return createSubqueryBuilder(result, listener, inExists, (AbstractCommonQueryBuilder<?, ?, ?, ?, ?>) subqueryBuilder, clause);
    }

    private <T> SubqueryBuilderImpl<T> createSubqueryBuilder(T result, SubqueryBuilderListener<T> listener, boolean inExists, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> builder, ClauseType clause) {
        SubqueryBuilderImpl<T> subqueryBuilder = new SubqueryBuilderImpl<T>(mainQuery, new QueryContext(queryBuilder, clause), aliasManager, parentJoinManager, mainQuery.subqueryExpressionFactory, result, false, listener);
        ExpressionCopyContext copyContext = subqueryBuilder.applyFrom(builder, builder.isMainQuery, !inExists, false, true, Collections.<ClauseType>emptySet(), Collections.<JoinNode>emptySet(), new IdentityHashMap<JoinManager, JoinManager>(), ExpressionCopyContext.EMPTY);

        if (inExists) {
            subqueryBuilder.selectManager.setDefaultSelect(null, Collections.singletonList(new SelectInfo(mainQuery.expressionFactory.createSimpleExpression("1"))), copyContext);
        }

        subqueryBuilder.collectParameters();

        if (listener != null) {
            listener.onBuilderStarted(subqueryBuilder);
        }

        return subqueryBuilder;
    }

    public <T extends Expression> T reattachSubqueries(T expression, ClauseType clauseType) {
        subqueryTransformationVisitor.clauseType = clauseType;
        return (T) expression.accept(subqueryTransformationVisitor);
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    class SubqueryReattachingTransformationVisitor extends InplaceModificationResultVisitorAdapter {

        private boolean inExists;
        private ClauseType clauseType;

        @Override
        public Expression visit(ExistsPredicate predicate) {
            inExists = true;
            try {
                return super.visit(predicate);
            } finally {
                inExists = false;
            }
        }

        @Override
        public Expression visit(SubqueryExpression expression) {
            AbstractCommonQueryBuilder<?, ?, ?, ?, ?> subqueryBuilder = (AbstractCommonQueryBuilder<?, ?, ?, ?, ?>) expression.getSubquery();
            SubqueryBuilderImpl<?> builder = createSubqueryBuilder(null, null, inExists, subqueryBuilder, clauseType);
            return new SubqueryExpression(builder);
        }
    }

}
