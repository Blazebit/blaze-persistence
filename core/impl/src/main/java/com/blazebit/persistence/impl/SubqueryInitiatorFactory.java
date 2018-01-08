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

package com.blazebit.persistence.impl;

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.InplaceModificationResultVisitorAdapter;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.predicate.ExistsPredicate;

import java.util.Arrays;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class SubqueryInitiatorFactory {

    private final MainQuery mainQuery;
    private final AliasManager aliasManager;
    private final JoinManager parentJoinManager;
    private final SubqueryReattachingTransformationVisitor subqueryTransformationVisitor;

    public SubqueryInitiatorFactory(MainQuery mainQuery, AliasManager aliasManager, JoinManager parentJoinManager) {
        this.mainQuery = mainQuery;
        this.aliasManager = aliasManager;
        this.parentJoinManager = parentJoinManager;
        this.subqueryTransformationVisitor = new SubqueryReattachingTransformationVisitor();
    }

    public <T> SubqueryInitiator<T> createSubqueryInitiator(T result, SubqueryBuilderListener<T> listener, boolean inExists) {
        return new SubqueryInitiatorImpl<T>(mainQuery, aliasManager, parentJoinManager, result, listener, inExists);
    }

    public <T> SubqueryBuilderImpl<T> createSubqueryBuilder(T result, SubqueryBuilderListener<T> listener, boolean inExists, FullQueryBuilder<?, ?> criteriaBuilder) {
        // TODO: paginated criteria builder?
        return createSubqueryBuilder(result, listener, inExists, (AbstractCommonQueryBuilder<?, ?, ?, ?, ?>) criteriaBuilder);
    }

    public <T> SubqueryBuilderImpl<T> createSubqueryBuilder(T result, SubqueryBuilderListener<T> listener, boolean inExists, SubqueryBuilderImpl<?> subqueryBuilder) {
        return createSubqueryBuilder(result, listener, inExists, (AbstractCommonQueryBuilder<?, ?, ?, ?, ?>) subqueryBuilder);
    }

    private <T> SubqueryBuilderImpl<T> createSubqueryBuilder(T result, SubqueryBuilderListener<T> listener, boolean inExists, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> builder) {
        SubqueryBuilderImpl<T> subqueryBuilder = new SubqueryBuilderImpl<T>(mainQuery, aliasManager, parentJoinManager, mainQuery.subqueryExpressionFactory, result, listener);

        mainQuery.cteManager.applyFrom(builder.mainQuery.cteManager);
        subqueryBuilder.joinManager.applyFrom(builder.joinManager);
        subqueryBuilder.whereManager.applyFrom(builder.whereManager);
        subqueryBuilder.havingManager.applyFrom(builder.havingManager);
        subqueryBuilder.groupByManager.applyFrom(builder.groupByManager);
        subqueryBuilder.orderByManager.applyFrom(builder.orderByManager);

        subqueryBuilder.setFirstResult(builder.firstResult);
        subqueryBuilder.setMaxResults(builder.maxResults);

        // TODO: set operations?

        if (inExists) {
            subqueryBuilder.selectManager.setDefaultSelect(Arrays.asList(new SelectInfo(mainQuery.expressionFactory.createArithmeticExpression("1"))));
        } else {
            subqueryBuilder.selectManager.setDefaultSelect(builder.selectManager.getSelectInfos());
        }

        if (listener != null) {
            listener.onBuilderStarted(subqueryBuilder);
        }

        return subqueryBuilder;
    }

    public <T extends Expression> T reattachSubqueries(T expression) {
        return (T) expression.accept(subqueryTransformationVisitor);
    }

    class SubqueryReattachingTransformationVisitor extends InplaceModificationResultVisitorAdapter {

        private boolean inExists;

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
            SubqueryBuilderImpl<?> builder = createSubqueryBuilder(null, null, inExists, subqueryBuilder);
            return new SubqueryExpression(builder);
        }
    }

}
