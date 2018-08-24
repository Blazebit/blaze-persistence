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

import com.blazebit.persistence.FullSelectCTECriteriaBuilder;
import com.blazebit.persistence.LeafOngoingFinalSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.ReturningModificationCriteriaBuilderFactory;
import com.blazebit.persistence.SelectRecursiveCTECriteriaBuilder;
import com.blazebit.persistence.StartOngoingSetOperationCTECriteriaBuilder;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.1.0
 */
public class CTEManager extends CTEBuilderListenerImpl {

    private final MainQuery mainQuery;
    private final Map<Class<?>, CTEInfo> ctes;
    private QueryContext queryContext;
    private boolean recursive = false;

    CTEManager(MainQuery mainQuery) {
        this.mainQuery = mainQuery;
        this.ctes = new LinkedHashMap<>();
    }

    void init(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder) {
        if (queryContext == null) {
            this.queryContext = new QueryContext(queryBuilder, ClauseType.CTE);
        }
    }

    void applyFrom(CTEManager cteManager) {
        if (cteManager.recursive) {
            recursive = true;
        }
        for (Map.Entry<Class<?>, CTEInfo> entry : cteManager.ctes.entrySet()) {
            CTEInfo cteInfo = entry.getValue().copy(this);
            mainQuery.parameterManager.collectParameterRegistrations(cteInfo.nonRecursiveCriteriaBuilder, ClauseType.CTE);
            if (cteInfo.recursive) {
                mainQuery.parameterManager.collectParameterRegistrations(cteInfo.recursiveCriteriaBuilder, ClauseType.CTE);
            }

            ctes.put(entry.getKey(), cteInfo);
        }
    }

    QueryContext getQueryContext() {
        return queryContext;
    }

    Collection<CTEInfo> getCtes() {
        return ctes.values();
    }

    public CTEInfo getCte(Class<?> cteType) {
        return ctes.get(cteType);
    }

    public boolean hasCtes() {
        return ctes.size() > 0;
    }

    boolean isRecursive() {
        return recursive;
    }

    void buildClause(StringBuilder sb) {
        if (ctes.isEmpty()) {
            return;
        }

        sb.append("WITH ");
        
        if (recursive) {
            sb.append("RECURSIVE ");
        }
        
        boolean first = true;
        for (CTEInfo cte : ctes.values()) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            
            sb.append(cte.name);
            sb.append('(');

            final List<String> attributes = cte.attributes;
            sb.append(attributes.get(0));
            
            for (int i = 1; i < attributes.size(); i++) {
                sb.append(", ");
                sb.append(attributes.get(i));
            }

            sb.append(')');
            
            sb.append(" AS(\n");
            sb.append(cte.nonRecursiveCriteriaBuilder.getQueryString());
            
            if (cte.recursive) {
                sb.append("\nUNION ALL\n");
                sb.append(cte.recursiveCriteriaBuilder.getQueryString());
            }
            
            sb.append("\n)");
        }
        
        sb.append("\n");
    }

    @SuppressWarnings("unchecked")
    <Y> StartOngoingSetOperationCTECriteriaBuilder<Y, LeafOngoingFinalSetOperationCTECriteriaBuilder<Y>> withStartSet(Class<?> cteClass, Y result) {
        String cteName = cteClass.getSimpleName();
        FinalSetOperationCTECriteriaBuilderImpl<Y> parentFinalSetOperationBuilder = new FinalSetOperationCTECriteriaBuilderImpl<Y>(mainQuery, queryContext, (Class<Y>) cteClass, result, null, false, this, null);
        OngoingFinalSetOperationCTECriteriaBuilderImpl<Y> subFinalSetOperationBuilder = new OngoingFinalSetOperationCTECriteriaBuilderImpl<Y>(mainQuery, queryContext, (Class<Y>) cteClass, null, null, true, parentFinalSetOperationBuilder.getSubListener(), null);
        this.onBuilderStarted(parentFinalSetOperationBuilder);
        
        LeafOngoingSetOperationCTECriteriaBuilderImpl<Y> leafCb = new LeafOngoingSetOperationCTECriteriaBuilderImpl<Y>(mainQuery, queryContext, cteName, (Class<Object>) cteClass, result, parentFinalSetOperationBuilder.getSubListener(), (FinalSetOperationCTECriteriaBuilderImpl<Object>) parentFinalSetOperationBuilder);
        StartOngoingSetOperationCTECriteriaBuilderImpl<Y, LeafOngoingSetOperationCTECriteriaBuilderImpl<Y>> cb = new StartOngoingSetOperationCTECriteriaBuilderImpl<Y, LeafOngoingSetOperationCTECriteriaBuilderImpl<Y>>(
                mainQuery, queryContext, cteName, (Class<Object>) cteClass, result, subFinalSetOperationBuilder.getSubListener(), (OngoingFinalSetOperationCTECriteriaBuilderImpl<Object>) subFinalSetOperationBuilder, leafCb
        );
        
        subFinalSetOperationBuilder.setOperationManager.setStartQueryBuilder(cb);
        parentFinalSetOperationBuilder.setOperationManager.setStartQueryBuilder(subFinalSetOperationBuilder);

        subFinalSetOperationBuilder.getSubListener().onBuilderStarted(cb);
        parentFinalSetOperationBuilder.getSubListener().onBuilderStarted(leafCb);

        // Generics hell..
        return (StartOngoingSetOperationCTECriteriaBuilder) cb;
    }

    @SuppressWarnings("unchecked")
    <Y> FullSelectCTECriteriaBuilder<Y> with(Class<?> cteClass, Y result) {
        String cteName = cteClass.getSimpleName();
        FullSelectCTECriteriaBuilderImpl<Y> cteBuilder = new FullSelectCTECriteriaBuilderImpl<Y>(mainQuery, queryContext, cteName, (Class<Object>) cteClass, result, this);
        this.onBuilderStarted(cteBuilder);
        return cteBuilder;
    }

    @SuppressWarnings("unchecked")
    <Y> SelectRecursiveCTECriteriaBuilder<Y> withRecursive(Class<?> cteClass, Y result) {
        String cteName = cteClass.getSimpleName();
        recursive = true;
        RecursiveCTECriteriaBuilderImpl<Y> cteBuilder = new RecursiveCTECriteriaBuilderImpl<Y>(mainQuery, queryContext, cteName, (Class<Object>) cteClass, result, this);
        this.onBuilderStarted(cteBuilder);
        return cteBuilder;
    }

    <Y> ReturningModificationCriteriaBuilderFactory<Y> withReturning(Class<?> cteClass, Y result) {
        String cteName = cteClass.getSimpleName();
        ReturningModificationCriteraBuilderFactoryImpl<Y> factory = new ReturningModificationCriteraBuilderFactoryImpl<Y>(mainQuery, cteName, cteClass, result, this);
        return factory;
    }

    @Override
    public void onBuilderEnded(CTEInfoBuilder builder) {
        super.onBuilderEnded(builder);
        CTEInfo cteInfo = builder.createCTEInfo();
        mainQuery.parameterManager.collectParameterRegistrations(cteInfo.nonRecursiveCriteriaBuilder, ClauseType.CTE);
        if (cteInfo.recursive) {
            mainQuery.parameterManager.collectParameterRegistrations(cteInfo.recursiveCriteriaBuilder, ClauseType.CTE);
        }
        ctes.put(cteInfo.cteType.getJavaType(), cteInfo);
    }

}
