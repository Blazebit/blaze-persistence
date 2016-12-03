/*
 * Copyright 2014 - 2016 Blazebit.
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

import java.util.Arrays;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class SubqueryInitiatorFactory {

    private final MainQuery mainQuery;
    private final AliasManager aliasManager;
    private final JoinManager parentJoinManager;

    public SubqueryInitiatorFactory(MainQuery mainQuery, AliasManager aliasManager, JoinManager parentJoinManager) {
        this.mainQuery = mainQuery;
        this.aliasManager = aliasManager;
        this.parentJoinManager = parentJoinManager;
    }

    public <T> SubqueryInitiator<T> createSubqueryInitiator(T result, SubqueryBuilderListener<T> listener, boolean inExists) {
        return new SubqueryInitiatorImpl<T>(mainQuery, aliasManager, parentJoinManager, result, listener, inExists);
    }

    public <T> SubqueryBuilderImpl<T> createSubqueryBuilder(T result, SubqueryBuilderListener<T> listener, boolean inExists, FullQueryBuilder<?, ?> criteriaBuilder) {
        AbstractFullQueryBuilder<?, ?, ?, ?, ?> builder = (AbstractFullQueryBuilder<?, ?, ?, ?, ?>) criteriaBuilder;

        SubqueryBuilderImpl<T> subqueryBuilder = new SubqueryBuilderImpl<T>(mainQuery, aliasManager, parentJoinManager, mainQuery.subqueryExpressionFactory, result, listener);

        mainQuery.cteManager.applyFrom(builder.mainQuery.cteManager);
        subqueryBuilder.joinManager.applyFrom(builder.joinManager);
        subqueryBuilder.whereManager.applyFrom(builder.whereManager);
        subqueryBuilder.havingManager.applyFrom(builder.havingManager);
        subqueryBuilder.groupByManager.applyFrom(builder.groupByManager);
        subqueryBuilder.orderByManager.applyFrom(builder.orderByManager);

        subqueryBuilder.setFirstResult(builder.firstResult);
        subqueryBuilder.setFirstResult(builder.maxResults);

        // TODO: set operations? paginated criteria builder?

        if (inExists) {
            subqueryBuilder.selectManager.setDefaultSelect(Arrays.asList(new SelectInfo(mainQuery.expressionFactory.createArithmeticExpression("1"))));
        } else {
            subqueryBuilder.selectManager.setDefaultSelect(builder.selectManager.getSelectInfos());
        }

        listener.onBuilderStarted(subqueryBuilder);
        return subqueryBuilder;
    }
}
