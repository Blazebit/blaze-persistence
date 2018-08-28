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

import com.blazebit.persistence.BaseCTECriteriaBuilder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.impl.query.CTEQuerySpecification;
import com.blazebit.persistence.impl.query.CustomSQLQuery;
import com.blazebit.persistence.impl.query.EntityFunctionNode;
import com.blazebit.persistence.impl.query.QuerySpecification;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.NullExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PropertyExpression;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.spi.JpaMetamodelAccessor;
import com.blazebit.persistence.spi.SetOperationType;

import javax.persistence.Query;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

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
    protected final Map<String, ExtendedAttribute> attributeEntries;
    protected final Map<String, Integer> bindingMap;
    protected final Map<String, String> columnBindingMap;
    protected final CTEBuilderListenerImpl subListener;
    private CTEInfo info;

    public AbstractCTECriteriaBuilder(MainQuery mainQuery, QueryContext queryContext, String cteName, Class<Object> clazz, Y result, CTEBuilderListener listener, BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> finalSetOperationBuilder) {
        super(mainQuery, queryContext, false, DbmsStatementType.SELECT, clazz, null, finalSetOperationBuilder, false);
        this.result = result;
        this.listener = listener;

        this.cteType = mainQuery.metamodel.entity(clazz);
        this.attributeEntries = mainQuery.metamodel.getManagedType(ExtendedManagedType.class, clazz).getAttributes();
        this.cteName = cteName;
        this.bindingMap = new LinkedHashMap<String, Integer>(attributeEntries.size());
        this.columnBindingMap = new LinkedHashMap<String, String>(attributeEntries.size());
        this.subListener = new CTEBuilderListenerImpl();
    }

    public AbstractCTECriteriaBuilder(AbstractCTECriteriaBuilder<Y, X, Z, W> builder, MainQuery mainQuery, QueryContext queryContext) {
        super(builder, mainQuery, queryContext);
        this.result = null;
        this.listener = null;

        this.cteType = builder.cteType;
        this.attributeEntries = builder.attributeEntries;
        this.cteName = builder.cteName;
        this.bindingMap = new LinkedHashMap<>(builder.bindingMap);
        this.columnBindingMap = new LinkedHashMap<>(builder.columnBindingMap);
        this.subListener = null;
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
        // NOTE: This must happen first because it generates implicit joins
        String baseQueryString = getBaseQueryStringWithCheck();
        Set<JoinNode> keyRestrictedLeftJoins = joinManager.getKeyRestrictedLeftJoins();
        Query query;
        
        if (hasLimit() || joinManager.hasEntityFunctions() || !keyRestrictedLeftJoins.isEmpty()) {
            // We need to change the underlying sql when doing a limit
            query = em.createQuery(baseQueryString);

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
                    parameterManager.getParameters(),
                    parameterListNames,
                    limit,
                    offset,
                    keyRestrictedLeftJoinAliases,
                    entityFunctionNodes
            );

            query = new CustomSQLQuery(
                    querySpecification,
                    query,
                    parameterManager.getTransformers(),
                    parameterManager.getValuesParameters(),
                    parameterManager.getValuesBinders()
            );
        } else {
            query = em.createQuery(baseQueryString);
        }

        parameterManager.parameterizeQuery(query);
        return query;
    }

    public SelectBuilder<X> bind(String cteAttribute) {
        ExtendedAttribute attributeEntry = attributeEntries.get(cteAttribute);
        
        if (attributeEntry == null) {
            throw new IllegalArgumentException("The cte attribute [" + cteAttribute + "] does not exist!");
        }
        // TODO: move this check and a type check to end()
//        if (attributeEntry.getAttribute().getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
//            throw new IllegalArgumentException("Can't bind the embeddable cte attribute [" + cteAttribute + "] directly! Please bind the respective sub attributes.");
//        }
        if (bindingMap.put(cteAttribute, selectManager.getSelectInfos().size()) != null) {
            throw new IllegalArgumentException("The cte attribute [" + cteAttribute + "] has already been bound!");
        }
        for (String column : attributeEntry.getColumnNames()) {
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
        final Queue<String> attributeQueue = new ArrayDeque<>(bindingMap.keySet());
        while (!attributeQueue.isEmpty()) {
            final String attributeName = attributeQueue.remove();
            Integer tupleIndex = bindingMap.get(attributeName);

            final ExtendedAttribute attributeEntry = attributeEntries.get(attributeName);
            final List<Attribute<?, ?>> attributePath = attributeEntry.getAttributePath();
            final JpaMetamodelAccessor jpaMetamodelAccessor = mainQuery.jpaProvider.getJpaMetamodelAccessor();
            final Attribute<?, ?> lastAttribute = attributePath.get(attributePath.size() - 1);

            if (jpaMetamodelAccessor.isJoinable(lastAttribute) || lastAttribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
                // We have to map *-to-one relationships to their id or unique props
                // NOTE: Since we are talking about *-to-ones, the expression can only be a path to an object
                // so it is safe to just append the id to the path
                Expression selectExpression = selectManager.getSelectInfos().get(tupleIndex).getExpression();

                // TODO: Maybe also allow Treat, Case-When, Array?
                if (selectExpression instanceof NullExpression) {
                    // When binding null, we don't have to adapt anything
                } else if (selectExpression instanceof PathExpression) {
                    boolean firstBinding = true;
                    final Collection<String> embeddedPropertyNames;

                    if (lastAttribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
                        embeddedPropertyNames = new TreeSet<>(JpaMetamodelUtils.getEmbeddedPropertyNames((EmbeddableType<?>) ((SingularAttribute<?, ?>) lastAttribute).getType()));
                    } else {
                        embeddedPropertyNames = new TreeSet<>(cbf.getJpaProvider().getIdentifierOrUniqueKeyEmbeddedPropertyNames(cteType, attributeName));
                    }

                    PathExpression baseExpression = embeddedPropertyNames.size() > 1 ?
                            ((PathExpression) selectExpression).clone(false) : ((PathExpression) selectExpression);

                    joinManager.implicitJoin(baseExpression, true, null, ClauseType.SELECT, null, false, false, false, false);

                    if (baseExpression.getPathReference().getType().getPersistenceType().equals(Type.PersistenceType.BASIC)) {
                        throw new IllegalStateException("An association should be bound to its association type and not its identifier type");
                    }

                    bindingMap.remove(attributeName);

                    for (String embeddedPropertyName : embeddedPropertyNames) {
                        PathExpression pathExpression = firstBinding ?
                                ((PathExpression) selectExpression) : baseExpression.clone(false);

                        pathExpression.getExpressions().add(new PropertyExpression(embeddedPropertyName));
                        String nestedAttributePath = attributeName + "." + embeddedPropertyName;
                        ExtendedAttribute<?, ?> nestedAttributeEntry = attributeEntries.get(nestedAttributePath);

                        // Process the nested attribute path recursively
                        attributeQueue.add(nestedAttributePath);

                        // Replace this binding in the binding map, additional selects need an updated index
                        bindingMap.put(nestedAttributePath, firstBinding ? tupleIndex : selectManager.getSelectInfos().size());

                        if (!firstBinding) {
                            selectManager.select(pathExpression, null);
                        } else {
                            firstBinding = false;
                        }

                        for (String column : nestedAttributeEntry.getColumnNames()) {
                            columnBindingMap.put(column, nestedAttributePath);
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Illegal expression '" + selectExpression.toString() + "' for binding relation '" + attributeName + "'!");
                }
            }
        }

        String[] attributes = new String[bindingMap.size()];
        for (Map.Entry<String, Integer> entry : bindingMap.entrySet()) {
            attributes[entry.getValue()] = entry.getKey();
        }
        return Arrays.asList(attributes);
    }

    protected List<String> prepareAndGetColumnNames() {
        StringBuilder sb = null;
        for (ExtendedAttribute entry : attributeEntries.values()) {
            for (String column : entry.getColumnNames()) {
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

        String[] columns = new String[columnBindingMap.size()];
        for (Map.Entry<String, String> columnBinding : columnBindingMap.entrySet()) {
            columns[bindingMap.get(columnBinding.getValue())] = columnBinding.getKey();
        }
        return Arrays.asList(columns);
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
            return new OngoingFinalSetOperationCTECriteriaBuilderImpl<Object>(mainQuery, queryContext, (Class<Object>) cteType.getJavaType(), newResult, operator, nested, newListener, initiator);
        } else {
            return new FinalSetOperationCTECriteriaBuilderImpl<Object>(mainQuery, queryContext, (Class<Object>) cteType.getJavaType(), newResult, operator, nested, newListener, initiator);
        }
    }

    @SuppressWarnings("unchecked")
    protected LeafOngoingSetOperationCTECriteriaBuilderImpl<Y> createLeaf(BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> finalSetOperationBuilder) {
        CTEBuilderListener newListener = finalSetOperationBuilder.getSubListener();
        LeafOngoingSetOperationCTECriteriaBuilderImpl<Y> next = new LeafOngoingSetOperationCTECriteriaBuilderImpl<Y>(mainQuery, queryContext, cteName, (Class<Object>) cteType.getJavaType(), result, newListener, (FinalSetOperationCTECriteriaBuilderImpl<Object>) finalSetOperationBuilder);
        newListener.onBuilderStarted(next);
        return next;
    }

    @SuppressWarnings("unchecked")
    protected <T extends AbstractCommonQueryBuilder<?, ?, ?, ?, ?>> StartOngoingSetOperationCTECriteriaBuilderImpl<Y, T> createStartOngoing(BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> finalSetOperationBuilder, T endSetResult) {
        // TODO: This is such an ugly hack, but I don't know how else to fix this generics issue for now
        finalSetOperationBuilder.setEndSetResult((T) endSetResult);
        
        CTEBuilderListener newListener = finalSetOperationBuilder.getSubListener();
        StartOngoingSetOperationCTECriteriaBuilderImpl<Y, T> next = new StartOngoingSetOperationCTECriteriaBuilderImpl<Y, T>(mainQuery, queryContext, cteName, (Class<Object>) cteType.getJavaType(), result, newListener, (OngoingFinalSetOperationCTECriteriaBuilderImpl<Object>) finalSetOperationBuilder, endSetResult);
        newListener.onBuilderStarted(next);
        return next;
    }

    @SuppressWarnings("unchecked")
    protected <T extends AbstractCommonQueryBuilder<?, ?, ?, ?, ?>> OngoingSetOperationCTECriteriaBuilderImpl<Y, T> createOngoing(BaseFinalSetOperationCTECriteriaBuilderImpl<Object, ?> finalSetOperationBuilder, T endSetResult) {
        // TODO: This is such an ugly hack, but I don't know how else to fix this generics issue for now
        finalSetOperationBuilder.setEndSetResult((T) endSetResult);

        CTEBuilderListener newListener = finalSetOperationBuilder.getSubListener();
        OngoingSetOperationCTECriteriaBuilderImpl<Y, T> next = new OngoingSetOperationCTECriteriaBuilderImpl<Y, T>(mainQuery, queryContext, cteName, (Class<Object>) cteType.getJavaType(), result, newListener, (OngoingFinalSetOperationCTECriteriaBuilderImpl<Object>) finalSetOperationBuilder, endSetResult);
        newListener.onBuilderStarted(next);
        return next;
    }

}
