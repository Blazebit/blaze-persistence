/*
 * Copyright 2014 - 2017 Blazebit.
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

import com.blazebit.persistence.BaseCTECriteriaBuilder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.NullExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PropertyExpression;
import com.blazebit.persistence.impl.query.CTEQuerySpecification;
import com.blazebit.persistence.impl.query.CustomSQLQuery;
import com.blazebit.persistence.impl.query.EntityFunctionNode;
import com.blazebit.persistence.impl.query.QuerySpecification;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.SetOperationType;

import javax.persistence.Query;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @param <Y> The criteria builder returned after the cte builder
 * @param <X> The concrete builder type
 * @param <Z> The builder type that should be returned on set operations
 * @author Christian Beikov
 * @since 1.1.0
 */
public abstract class AbstractCTECriteriaBuilder<Y, X extends BaseCTECriteriaBuilder<X>, Z, W> extends AbstractCommonQueryBuilder<Object, X, Z, W, BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?>> implements BaseCTECriteriaBuilder<X>, SelectBuilder<X>, CTEInfoBuilder {
    
    protected static final Integer EMPTY = Integer.valueOf(-1);
    protected final Y result;
    protected final CTEBuilderListener listener;
    protected final String cteName;
    protected final EntityType<?> cteType;
    protected final Map<String, Map.Entry<AttributePath, String[]>> attributeColumnMappings;
    protected final Map<String, Integer> bindingMap;
    protected final Map<String, String> columnBindingMap;
    protected final CTEBuilderListenerImpl subListener;
    private CTEInfo info;

    public AbstractCTECriteriaBuilder(MainQuery mainQuery, String cteName, Class<Object> clazz, Y result, CTEBuilderListener listener, BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> finalSetOperationBuilder) {
        super(mainQuery, false, DbmsStatementType.SELECT, clazz, null, finalSetOperationBuilder, false);
        this.result = result;
        this.listener = listener;

        this.cteType = mainQuery.metamodel.entity(clazz);
        this.attributeColumnMappings = mainQuery.metamodel.getAttributeColumnNameMapping(clazz);
        this.cteName = cteName;
        this.bindingMap = new LinkedHashMap<String, Integer>(attributeColumnMappings.size());
        this.columnBindingMap = new LinkedHashMap<String, String>(attributeColumnMappings.size());
        this.subListener = new CTEBuilderListenerImpl();
    }
    
    public CTEBuilderListenerImpl getSubListener() {
        return subListener;
    }

    @Override
    protected void buildExternalQueryString(StringBuilder sbSelectFrom) {
        buildBaseQueryString(sbSelectFrom, true);
    }

    @Override
    protected Query getQuery() {
        Set<JoinNode> keyRestrictedLeftJoins = joinManager.getKeyRestrictedLeftJoins();
        Query query;
        
        if (hasLimit() || joinManager.hasEntityFunctions() || !keyRestrictedLeftJoins.isEmpty()) {
            // We need to change the underlying sql when doing a limit
            query = em.createQuery(getBaseQueryStringWithCheck());

            Set<String> parameterListNames = parameterManager.getParameterListNames(query);
            String limit = null;
            String offset = null;

            // The main query will handle that separately
            if (!isMainQuery) {
                if (firstResult != 0) {
                    offset = Integer.toString(firstResult);
                }
                if (maxResults != Integer.MAX_VALUE) {
                    limit = Integer.toString(maxResults);
                }
            }

            List<String> keyRestrictedLeftJoinAliases = getKeyRestrictedLeftJoinAliases(query, keyRestrictedLeftJoins, Collections.EMPTY_SET);
            List<EntityFunctionNode> entityFunctionNodes = getEntityFunctionNodes(query);

            QuerySpecification querySpecification = new CTEQuerySpecification(
                    this,
                    query,
                    parameterListNames,
                    limit,
                    offset,
                    keyRestrictedLeftJoinAliases,
                    entityFunctionNodes
            );

            query = new CustomSQLQuery(
                    querySpecification,
                    query,
                    parameterManager.getValuesParameters(),
                    parameterManager.getValuesBinders()
            );
        } else {
            query = em.createQuery(getBaseQueryStringWithCheck());
        }

        parameterManager.parameterizeQuery(query);
        return query;
    }

    public SelectBuilder<X> bind(String cteAttribute) {
        Map.Entry<AttributePath, String[]> attributeEntry = attributeColumnMappings.get(cteAttribute);
        
        if (attributeEntry == null) {
            if (cteType.getAttribute(cteAttribute) != null) {
                throw new IllegalArgumentException("Can't bind the embeddable cte attribute [" + cteAttribute + "] directly! Please bind the respective sub attributes.");
            }
            throw new IllegalArgumentException("The cte attribute [" + cteAttribute + "] does not exist!");
        }
        if (bindingMap.put(cteAttribute, selectManager.getSelectInfos().size()) != null) {
            throw new IllegalArgumentException("The cte attribute [" + cteAttribute + "] has already been bound!");
        }
        for (String column : attributeEntry.getValue()) {
            if (columnBindingMap.put(column, cteAttribute) != null) {
                throw new IllegalArgumentException("The cte column [" + column + "] has already been bound!");
            }
        }
        
        return this;
    }

    public Y end() {
        listener.onBuilderEnded(this);
        return result;
    }

    @Override
    protected void prepareAndCheck() {
        if (!needsCheck) {
            return;
        }
        try {
            List<String> attributes = prepareAndGetAttributes();
            List<String> columns = prepareAndGetColumnNames();
            super.prepareAndCheck();
            info = new CTEInfo(cteName, cteType, attributes, columns, false, false, this, null);
        } catch (RuntimeException ex) {
            needsCheck = true;
            throw ex;
        }
    }

    public CTEInfo createCTEInfo() {
        prepareAndCheck();
        return info;
    }

    protected List<String> prepareAndGetAttributes() {
        List<String> attributes = new ArrayList<String>(bindingMap.size());
        for (Map.Entry<String, Integer> bindingEntry : bindingMap.entrySet()) {
            final String attributeName = bindingEntry.getKey();

            AttributePath attributePath = attributeColumnMappings.get(attributeName).getKey();
            attributes.add(attributeName);

            if (JpaUtils.isJoinable(attributePath.getAttributes().get(attributePath.getAttributes().size() - 1))) {
                // We have to map *-to-one relationships to their ids
                EntityType<?> type = mainQuery.metamodel.entity(attributePath.getAttributeClass());
                Attribute<?, ?> idAttribute = JpaUtils.getIdAttribute(type);
                // NOTE: Since we are talking about *-to-ones, the expression can only be a path to an object
                // so it is safe to just append the id to the path
                Expression selectExpression = selectManager.getSelectInfos().get(bindingEntry.getValue()).getExpression();

                // TODO: Maybe also allow Treat, Case-When, Array?
                if (selectExpression instanceof NullExpression) {
                    // When binding null, we don't have to adapt anything
                } else if (selectExpression instanceof PathExpression) {
                    PathExpression pathExpression = (PathExpression) selectExpression;
                    // Only append the id if it's not already there
                    if (!idAttribute.getName().equals(pathExpression.getExpressions().get(pathExpression.getExpressions().size() - 1).toString())) {
                        pathExpression.getExpressions().add(new PropertyExpression(idAttribute.getName()));
                    }
                } else {
                    throw new IllegalArgumentException("Illegal expression '" + selectExpression.toString() + "' for binding relation '" + attributeName + "'!");
                }
            }
        }

        return attributes;
    }

    protected List<String> prepareAndGetColumnNames() {
        StringBuilder sb = null;
        for (Map.Entry<AttributePath, String[]> entry : attributeColumnMappings.values()) {
            for (String column : entry.getValue()) {
                if (!columnBindingMap.containsKey(column)) {
                    if (sb == null) {
                        sb = new StringBuilder();
                        sb.append("[");
                    } else {
                        sb.append(", ");
                    }

                    sb.append(column);
                }
            }
        }

        if (sb != null) {
            sb.insert(0, "The following column names have not been bound: ");
            sb.append("]");
            throw new IllegalStateException(sb.toString());
        }

        return new ArrayList<>(columnBindingMap.keySet());
    }
    
    protected BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> createFinalSetOperationBuilder(SetOperationType operator, boolean nested, boolean isSubquery) {
        FullSelectCTECriteriaBuilderImpl<?> newInitiator = finalSetOperationBuilder == null ? null : finalSetOperationBuilder.getInitiator();
        return createFinalSetOperationBuilder(operator, nested, isSubquery, newInitiator);
    }
    
    @SuppressWarnings("unchecked")
    protected BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> createFinalSetOperationBuilder(SetOperationType operator, boolean nested, boolean isSubquery, FullSelectCTECriteriaBuilderImpl<?> initiator) {
        CTEBuilderListener newListener = finalSetOperationBuilder == null ? listener : finalSetOperationBuilder.getSubListener();
        Y newResult = finalSetOperationBuilder == null ? result : (Y) finalSetOperationBuilder.getResult();
        
        if (isSubquery) {
            return new OngoingFinalSetOperationCTECriteriaBuilderImpl<Object>(mainQuery, (Class<Object>) cteType.getJavaType(), newResult, operator, nested, newListener, initiator);
        } else {
            return new FinalSetOperationCTECriteriaBuilderImpl<Object>(mainQuery, (Class<Object>) cteType.getJavaType(), newResult, operator, nested, newListener, initiator);
        }
    }

    @SuppressWarnings("unchecked")
    protected LeafOngoingSetOperationCTECriteriaBuilderImpl<Y> createLeaf(BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> finalSetOperationBuilder) {
        CTEBuilderListener newListener = finalSetOperationBuilder.getSubListener();
        LeafOngoingSetOperationCTECriteriaBuilderImpl<Y> next = new LeafOngoingSetOperationCTECriteriaBuilderImpl<Y>(mainQuery, cteName, (Class<Object>) cteType.getJavaType(), result, newListener, (FinalSetOperationCTECriteriaBuilderImpl<Object>) finalSetOperationBuilder);
        newListener.onBuilderStarted(next);
        return next;
    }

    @SuppressWarnings("unchecked")
    protected <T extends AbstractCommonQueryBuilder<?, ?, ?, ?, ?>> StartOngoingSetOperationCTECriteriaBuilderImpl<Y, T> createStartOngoing(BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> finalSetOperationBuilder, T endSetResult) {
        // TODO: This is such an ugly hack, but I don't know how else to fix this generics issue for now
        finalSetOperationBuilder.setEndSetResult((T) endSetResult);
        
        CTEBuilderListener newListener = finalSetOperationBuilder.getSubListener();
        StartOngoingSetOperationCTECriteriaBuilderImpl<Y, T> next = new StartOngoingSetOperationCTECriteriaBuilderImpl<Y, T>(mainQuery, cteName, (Class<Object>) cteType.getJavaType(), result, newListener, (OngoingFinalSetOperationCTECriteriaBuilderImpl<Object>) finalSetOperationBuilder, endSetResult);
        newListener.onBuilderStarted(next);
        return next;
    }

    @SuppressWarnings("unchecked")
    protected <T extends AbstractCommonQueryBuilder<?, ?, ?, ?, ?>> OngoingSetOperationCTECriteriaBuilderImpl<Y, T> createOngoing(BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> finalSetOperationBuilder, T endSetResult) {
        // TODO: This is such an ugly hack, but I don't know how else to fix this generics issue for now
        finalSetOperationBuilder.setEndSetResult((T) endSetResult);

        CTEBuilderListener newListener = finalSetOperationBuilder.getSubListener();
        OngoingSetOperationCTECriteriaBuilderImpl<Y, T> next = new OngoingSetOperationCTECriteriaBuilderImpl<Y, T>(mainQuery, cteName, (Class<Object>) cteType.getJavaType(), result, newListener, (OngoingFinalSetOperationCTECriteriaBuilderImpl<Object>) finalSetOperationBuilder, endSetResult);
        newListener.onBuilderStarted(next);
        return next;
    }

}
