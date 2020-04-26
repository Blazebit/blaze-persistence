/*
 * Copyright 2014 - 2020 Blazebit.
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

import com.blazebit.persistence.BaseDeleteCriteriaBuilder;
import com.blazebit.persistence.parser.expression.ExpressionCopyContext;
import com.blazebit.persistence.spi.DbmsStatementType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public abstract class BaseDeleteCriteriaBuilderImpl<T, X extends BaseDeleteCriteriaBuilder<T, X>, Y> extends AbstractModificationCriteriaBuilder<T, X, Y> implements BaseDeleteCriteriaBuilder<T, X> {

    public BaseDeleteCriteriaBuilderImpl(MainQuery mainQuery, QueryContext queryContext, boolean isMainQuery, Class<T> clazz, String alias, CTEManager.CTEKey cteKey, Class<?> cteClass, Y result, CTEBuilderListener listener) {
        super(mainQuery, queryContext, isMainQuery, DbmsStatementType.DELETE, clazz, alias, cteKey, cteClass, result, listener);
    }

    public BaseDeleteCriteriaBuilderImpl(AbstractModificationCriteriaBuilder<T, X, Y> builder, MainQuery mainQuery, QueryContext queryContext, Map<JoinManager, JoinManager> joinManagerMapping, ExpressionCopyContext copyContext) {
        super(builder, mainQuery, queryContext, joinManagerMapping, copyContext);
    }

    @Override
    protected void buildBaseQueryString(StringBuilder sbSelectFrom, boolean externalRepresentation, JoinNode lateralJoinNode) {
        sbSelectFrom.append("DELETE FROM ");
        sbSelectFrom.append(entityType.getName()).append(' ');
        sbSelectFrom.append(entityAlias);
        JoinNode rootNode = joinManager.getRoots().get(0);
        if (joinManager.getRoots().size() > 1 || rootNode.hasChildNodes()) {
            sbSelectFrom.append(" WHERE EXISTS (SELECT 1");
            List<String> whereClauseConjuncts = new ArrayList<>();
            List<String> optionalWhereClauseConjuncts = new ArrayList<>();
            joinManager.buildClause(sbSelectFrom, Collections.<ClauseType>emptySet(), null, false, externalRepresentation, false, false, optionalWhereClauseConjuncts, whereClauseConjuncts, explicitVersionEntities, nodesToFetch, Collections.<JoinNode>emptySet(), rootNode);
            appendWhereClause(sbSelectFrom, externalRepresentation);
            sbSelectFrom.append(')');
        } else {
            appendWhereClause(sbSelectFrom, externalRepresentation);
        }
    }

}
