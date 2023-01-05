/*
 * Copyright 2014 - 2023 Blazebit.
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
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;

import javax.persistence.metamodel.EntityType;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
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
    private final Map<CTEKey, CTEInfo> ctes;
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

    void applyFrom(CTEManager cteManager, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        if (cteManager.recursive) {
            recursive = true;
        }
        for (Map.Entry<CTEKey, CTEInfo> entry : cteManager.ctes.entrySet()) {
            CTEInfo cteInfo = entry.getValue().copy(this, joinManagerMapping, copyContext);
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

    public boolean hasCte(EntityType<?> cte) {
        return ctes.containsKey(getCteKey(cte, null, null));
    }

    public CTEInfo getCte(EntityType<?> cteType) {
        return ctes.get(getCteKey(cteType, null, null));
    }

    public CTEInfo getCte(EntityType<?> cteType, String name, JoinManager joinManager) {
        return ctes.get(getCteKey(cteType, name, joinManager));
    }

    private CTEKey getCteKey(EntityType<?> cteClass, String name, JoinManager owner) {
        return new CTEKey(cteClass, name, owner);
    }

    private void assertCteNameAvailable(CTEKey cteKey) {
        if (ctes.containsKey(cteKey)) {
            throw new IllegalArgumentException("A CTE with the name " + cteKey.getName() + " already exists! Please choose a different type or name!");
        }
    }

    public boolean hasCtes() {
        if (ctes.size() > 0) {
            for (CTEInfo cteInfo : ctes.values()) {
                if (!cteInfo.inline) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean isRecursive() {
        return recursive;
    }

    void buildClause(StringBuilder sb) {
        if (ctes.isEmpty()) {
            return;
        }

        int startLength = sb.length();
        sb.append("WITH ");
        
        if (recursive) {
            sb.append("RECURSIVE ");
        }
        
        boolean first = true;
        for (CTEInfo cte : ctes.values()) {
            if (!cte.inline) {
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
                    sb.append(cte.unionAll ? "\nUNION ALL\n" : "\nUNION\n");
                    sb.append(cte.recursiveCriteriaBuilder.getQueryString());
                }

                sb.append("\n)");
            }
        }

        if (first) {
            sb.setLength(startLength);
        } else {
            sb.append("\n");
        }
    }

    @SuppressWarnings("unchecked")
    <Y> StartOngoingSetOperationCTECriteriaBuilder<Y, LeafOngoingFinalSetOperationCTECriteriaBuilder<Y>> withStartSet(EntityType<?> cteEntity, Y result, boolean inline, AliasManager parentAliasManager, JoinManager parentJoinManager) {
        mainQuery.assertSupportsAdvancedSql("Illegal use of WITH clause!", inline);
        Class<?> cteClass = cteEntity.getJavaType();
        CTEKey cteKey = getCteKey(cteEntity, null, null);
        FinalSetOperationCTECriteriaBuilderImpl<Y> parentFinalSetOperationBuilder = new FinalSetOperationCTECriteriaBuilderImpl<Y>(mainQuery, queryContext, (Class<Y>) cteClass, result, null, false, this, null);
        OngoingFinalSetOperationCTECriteriaBuilderImpl<Y> subFinalSetOperationBuilder = new OngoingFinalSetOperationCTECriteriaBuilderImpl<Y>(mainQuery, queryContext, (Class<Y>) cteClass, null, null, true, parentFinalSetOperationBuilder.getSubListener(), null);
        this.onBuilderStarted(parentFinalSetOperationBuilder);
        
        LeafOngoingSetOperationCTECriteriaBuilderImpl<Y> leafCb = new LeafOngoingSetOperationCTECriteriaBuilderImpl<Y>(mainQuery, queryContext, cteKey, inline, (Class<Object>) cteClass, result, parentFinalSetOperationBuilder.getSubListener(), (FinalSetOperationCTECriteriaBuilderImpl<Object>) parentFinalSetOperationBuilder, parentAliasManager, parentJoinManager);
        StartOngoingSetOperationCTECriteriaBuilderImpl<Y, LeafOngoingSetOperationCTECriteriaBuilderImpl<Y>> cb = new StartOngoingSetOperationCTECriteriaBuilderImpl<Y, LeafOngoingSetOperationCTECriteriaBuilderImpl<Y>>(
                mainQuery, queryContext, cteKey, inline, (Class<Object>) cteClass, result, subFinalSetOperationBuilder.getSubListener(), (OngoingFinalSetOperationCTECriteriaBuilderImpl<Object>) subFinalSetOperationBuilder, leafCb, parentAliasManager, parentJoinManager
        );
        
        subFinalSetOperationBuilder.setOperationManager.setStartQueryBuilder(cb);
        parentFinalSetOperationBuilder.setOperationManager.setStartQueryBuilder(subFinalSetOperationBuilder);

        subFinalSetOperationBuilder.getSubListener().onBuilderStarted(cb);
        parentFinalSetOperationBuilder.getSubListener().onBuilderStarted(leafCb);

        // Generics hell..
        return (StartOngoingSetOperationCTECriteriaBuilder) cb;
    }

    @SuppressWarnings("unchecked")
    <Y> FullSelectCTECriteriaBuilder<Y> with(EntityType<?> cteEntity, String name, Y result, boolean inline, JoinManager inlineOwner, AliasManager parentAliasManager, JoinManager parentJoinManager) {
        mainQuery.assertSupportsAdvancedSql("Illegal use of WITH clause!", inline);
        Class<?> cteClass = cteEntity.getJavaType();
        CTEKey cteKey = getCteKey(cteEntity, name, inlineOwner);
        assertCteNameAvailable(cteKey);
        FullSelectCTECriteriaBuilderImpl<Y> cteBuilder = new FullSelectCTECriteriaBuilderImpl<Y>(mainQuery, queryContext, cteKey, inline, (Class<Object>) cteClass, result, this, parentAliasManager, parentJoinManager);
        this.onBuilderStarted(cteBuilder);
        return cteBuilder;
    }

    @SuppressWarnings("unchecked")
    <Y> FullSelectCTECriteriaBuilder<Y> with(EntityType<?> cteEntity, Y result, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> builder, boolean inline, JoinManager inlineOwner, AliasManager parentAliasManager, JoinManager parentJoinManager) {
        mainQuery.assertSupportsAdvancedSql("Illegal use of WITH clause!", inline);
        Class<?> cteClass = cteEntity.getJavaType();
        CTEKey cteKey = getCteKey(cteEntity, null, inlineOwner);
        assertCteNameAvailable(cteKey);
        FullSelectCTECriteriaBuilderImpl<Y> cteBuilder = new FullSelectCTECriteriaBuilderImpl<Y>(mainQuery, queryContext, cteKey, inline, (Class<Object>) cteClass, result, this, parentAliasManager, parentJoinManager);
        cteBuilder.applyFrom(builder, true, false, false, true, Collections.<ClauseType>emptySet(), Collections.<JoinNode>emptySet(), new IdentityHashMap<JoinManager, JoinManager>(), ExpressionCopyContext.EMPTY);
        this.onBuilderStarted(cteBuilder);
        return cteBuilder;
    }

    @SuppressWarnings("unchecked")
    <Y> SelectRecursiveCTECriteriaBuilder<Y> withRecursive(EntityType<?> cteEntity, Y result) {
        mainQuery.assertSupportsAdvancedSql("Illegal use of WITH clause!");
        Class<?> cteClass = cteEntity.getJavaType();
        CTEKey cteKey = getCteKey(cteEntity, null, null);
        assertCteNameAvailable(cteKey);
        recursive = true;
        RecursiveCTECriteriaBuilderImpl<Y> cteBuilder = new RecursiveCTECriteriaBuilderImpl<Y>(mainQuery, queryContext, cteKey, (Class<Object>) cteClass, result, this);
        this.onBuilderStarted(cteBuilder);
        return cteBuilder;
    }

    <Y> ReturningModificationCriteriaBuilderFactory<Y> withReturning(EntityType<?> cteEntity, Y result) {
        mainQuery.assertSupportsAdvancedSql("Illegal use of WITH clause!");
        Class<?> cteClass = cteEntity.getJavaType();
        CTEKey cteKey = getCteKey(cteEntity, null, null);
        assertCteNameAvailable(cteKey);
        ReturningModificationCriteraBuilderFactoryImpl<Y> factory = new ReturningModificationCriteraBuilderFactoryImpl<Y>(mainQuery, cteKey, cteClass, result, this);
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
        ctes.put(new CTEKey(cteInfo.name, cteInfo.owner), cteInfo);
    }

    /**
     * @author Christian Beikov
     * @since 1.4.1
     */
    static final class CTEKey {
        private final String name;
        private final JoinManager owner;

        public CTEKey(EntityType<?> cteType, String name, JoinManager owner) {
            if (name == null) {
                this.name = JpaMetamodelUtils.getSimpleTypeName(cteType);
            } else {
                this.name = JpaMetamodelUtils.getSimpleTypeName(cteType) + "." + name;
            }
            this.owner = owner;
        }

        public CTEKey(String name, JoinManager owner) {
            this.name = name;
            this.owner = owner;
        }

        public String getName() {
            return name;
        }

        public JoinManager getOwner() {
            return owner;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof CTEKey)) {
                return false;
            }

            CTEKey cteKey = (CTEKey) o;

            if (name != null ? !name.equals(cteKey.name) : cteKey.name != null) {
                return false;
            }
            return owner != null ? owner.equals(cteKey.owner) : cteKey.owner == null;
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + (owner != null ? owner.hashCode() : 0);
            return result;
        }
    }
}
