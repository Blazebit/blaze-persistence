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

import java.util.*;

import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

import com.blazebit.persistence.BaseModificationCriteriaBuilder;
import com.blazebit.persistence.FullSelectCTECriteriaBuilder;
import com.blazebit.persistence.ReturningBuilder;
import com.blazebit.persistence.ReturningModificationCriteriaBuilderFactory;
import com.blazebit.persistence.ReturningObjectBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.SelectRecursiveCTECriteriaBuilder;
import com.blazebit.persistence.SimpleReturningBuilder;
import com.blazebit.persistence.impl.builder.object.ReturningTupleObjectBuilder;
import com.blazebit.persistence.impl.dialect.DB2DbmsDialect;
import com.blazebit.persistence.impl.query.CTENode;
import com.blazebit.persistence.impl.query.CustomReturningSQLTypedQuery;
import com.blazebit.persistence.impl.query.CustomSQLQuery;
import com.blazebit.persistence.impl.query.ModificationQuerySpecification;
import com.blazebit.persistence.impl.query.QuerySpecification;
import com.blazebit.persistence.impl.query.ReturningModificationQuerySpecification;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.DbmsStatementType;

/**
 *
 * @param <T> The entity type of this modification builder 
 * @author Christian Beikov
 * @since 1.1.0
 */
public abstract class AbstractModificationCriteriaBuilder<T, X extends BaseModificationCriteriaBuilder<X>, Y> extends AbstractCommonQueryBuilder<T, X, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, BaseFinalSetOperationBuilderImpl<T, ?, ?>> implements BaseModificationCriteriaBuilder<X>, CTEInfoBuilder, SimpleReturningBuilder {

    protected final EntityType<T> entityType;
    protected final String entityAlias;
    protected final EntityType<?> cteType;
    protected final String cteName;
    protected final Y result;
    protected final CTEBuilderListener listener;
    protected final boolean isReturningEntityAliasAllowed;
    protected final Map<String, List<Attribute<?, ?>>> returningAttributes;
    protected final Map<String, String> returningAttributeBindingMap;
    protected final Map<String, Map.Entry<AttributePath, String[]>> attributeColumnMappings;
    protected final Map<String, String> columnBindingMap;

    @SuppressWarnings("unchecked")
    public AbstractModificationCriteriaBuilder(MainQuery mainQuery, boolean isMainQuery, DbmsStatementType statementType, Class<T> clazz, String alias, String cteName, Class<?> cteClass, Y result, CTEBuilderListener listener) {
        // NOTE: using tuple here because this class is used for the join manager and tuple is definitively not an entity
        // but in case of the insert criteria, the appropriate return type which is convenient because update and delete don't have a return type
        super(mainQuery, isMainQuery, statementType, (Class<T>) Tuple.class, null);

        // set defaults
        if (alias != null) {
            // If the user supplies an alias, the intention is clear
            fromClassExplicitelySet = true;
        }
        
        this.entityType = mainQuery.metamodel.entity(clazz);
        this.entityAlias = joinManager.addRoot(entityType, alias);
        this.result = result;
        this.listener = listener;
        
        if (cteClass == null) {
            this.cteType = null;
            this.cteName = null;
            this.isReturningEntityAliasAllowed = false;
            this.returningAttributes = new LinkedHashMap<>(0);
            this.returningAttributeBindingMap = new LinkedHashMap<String, String>(0);
            this.attributeColumnMappings = null;
            this.columnBindingMap = null;
        } else {
            this.cteType = mainQuery.metamodel.entity(cteClass);
            this.cteName = cteName;
            // Returning the "entity" is only allowed in CTEs
            this.isReturningEntityAliasAllowed = true;
            this.returningAttributes = null;
            this.attributeColumnMappings = mainQuery.metamodel.getAttributeColumnNameMapping(cteClass);
            this.returningAttributeBindingMap = new LinkedHashMap<String, String>(attributeColumnMappings.size());
            this.columnBindingMap = new LinkedHashMap<String, String>(attributeColumnMappings.size());
        }
    }

    @Override
    public FullSelectCTECriteriaBuilder<X> with(Class<?> cteClass) {
        if (!dbmsDialect.supportsWithClauseInModificationQuery()) {
            throw new UnsupportedOperationException("The database does not support a with clause in modification queries!");
        }
        
        return super.with(cteClass);
    }

    @Override
    public SelectRecursiveCTECriteriaBuilder<X> withRecursive(Class<?> cteClass) {
        if (!dbmsDialect.supportsWithClauseInModificationQuery()) {
            throw new UnsupportedOperationException("The database does not support a with clause in modification queries!");
        }
        
        return super.withRecursive(cteClass);
    }

    @Override
    public ReturningModificationCriteriaBuilderFactory<X> withReturning(Class<?> cteClass) {
        if (!dbmsDialect.supportsWithClauseInModificationQuery()) {
            throw new UnsupportedOperationException("The database does not support a with clause in modification queries!");
        }
        
        return super.withReturning(cteClass);
    }
    
    protected void applyJpaReturning(StringBuilder sbSelectFrom) {
        sbSelectFrom.append(" RETURNING ");
        
        boolean first = true;
        for (String attribute : returningAttributeBindingMap.values()) {
            if (first) {
                first = false;
            } else {
                sbSelectFrom.append(", ");
            }
            
            sbSelectFrom.append(attribute);
        }
    }

    @Override
    public Query getQuery() {
        return getQuery(null);
    }

    @Override
    protected Query getQuery(Map<DbmsModificationState, String> includedModificationStates) {
        Query query;

        // TODO: Oracle requires everything except the sequence to be wrapped in a derived table
        // see https://github.com/Blazebit/blaze-persistence/issues/306

        // We use this to make these features only available to Hibernate as it is the only provider that supports sql replace yet
        if (statementType == DbmsStatementType.INSERT
                || (hasLimit() || mainQuery.cteManager.hasCtes() || returningAttributeBindingMap.size() > 0)) {

            // We need to change the underlying sql when doing a limit with hibernate since it does not support limiting insert ... select statements
            // For CTEs we will also need to change the underlying sql
            query = em.createQuery(getBaseQueryStringWithCheck());
            Set<String> parameterListNames = parameterManager.getParameterListNames(query);

            boolean isEmbedded = this instanceof ReturningBuilder;
            String[] returningColumns = getReturningColumns();
            boolean shouldRenderCteNodes = renderCteNodes(isEmbedded);
            List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(query, isEmbedded) : Collections.EMPTY_LIST;

            QuerySpecification querySpecification = new ModificationQuerySpecification(
                    this,
                    query,
                    getCountExampleQuery(),
                    parameterListNames,
                    mainQuery.cteManager.isRecursive(),
                    ctes,
                    shouldRenderCteNodes,
                    isEmbedded,
                    returningColumns,
                    includedModificationStates,
                    returningAttributeBindingMap
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

        // Don't set the values for UPDATE or DELETE statements, otherwise Datanucleus will pass through the values to the JDBC statement
        if (statementType == DbmsStatementType.INSERT) {
            query.setFirstResult(firstResult);
            query.setMaxResults(maxResults);
        }

        return query;
    }

    public int executeUpdate() {
        return getQuery().executeUpdate();
    }
    
    @Override
    protected Map<DbmsModificationState, String> getModificationStates(Map<Class<?>, Map<String, DbmsModificationState>> explicitVersionEntities) {
        Map<String, DbmsModificationState> versionEntities = explicitVersionEntities.get(entityType.getJavaType());
        
        if (versionEntities == null) {
            return null;
        }

        Map<DbmsModificationState, String> includedModificationStates = new HashMap<DbmsModificationState, String>();
        // TODO: this needs to include the modification states based on what the dbms uses as default
        boolean defaultOld = !(dbmsDialect instanceof DB2DbmsDialect);
        
        if (defaultOld) {
            for (Map.Entry<String, DbmsModificationState> entry : versionEntities.entrySet()) {
                if (entry.getValue() == DbmsModificationState.NEW) {
                    includedModificationStates.put(DbmsModificationState.NEW, entityType.getName() + "_new");
                    if (getStatementType() == DbmsStatementType.DELETE) {
                        includedModificationStates.put(DbmsModificationState.OLD, cteName);
                    }
                    break;
                }
            }
        } else {
            for (Map.Entry<String, DbmsModificationState> entry : versionEntities.entrySet()) {
                if (entry.getValue() == DbmsModificationState.OLD) {
                    includedModificationStates.put(DbmsModificationState.OLD, entityType.getName() + "_old");
                    break;
                }
            }
        }
        
        return includedModificationStates;
    }
    
    protected Map<String, String> getModificationStateRelatedTableNameRemappings(Map<Class<?>, Map<String, DbmsModificationState>> explicitVersionEntities) {
        Map<String, DbmsModificationState> versionEntities = explicitVersionEntities.get(entityType.getJavaType());
        
        if (versionEntities == null) {
            return null;
        }

        Map<String, String> tableNameRemappings = new HashMap<String, String>();
        // TODO: this needs to include the modification states based on what the dbms uses as default, so use a DbmsDialect method
        boolean defaultOld = !(dbmsDialect instanceof DB2DbmsDialect);
        
        if (defaultOld) {
            for (Map.Entry<String, DbmsModificationState> entry : versionEntities.entrySet()) {
                if (entry.getValue() == DbmsModificationState.NEW) {
                    tableNameRemappings.put(entry.getKey(), entityType.getName() + "_new");
                }
            }
        } else {
            for (Map.Entry<String, DbmsModificationState> entry : versionEntities.entrySet()) {
                if (entry.getValue() == DbmsModificationState.OLD) {
                    tableNameRemappings.put(entry.getKey(), entityType.getName() + "_old");
                }
            }
        }
        
        return tableNameRemappings;
    }

    public ReturningResult<Tuple> executeWithReturning(String... attributes) {
        return getWithReturningQuery(attributes).getSingleResult();
    }

    public TypedQuery<ReturningResult<Tuple>> getWithReturningQuery(String... attributes) {
        if (attributes == null) {
            throw new NullPointerException("attributes");
        }
        if (attributes.length == 0) {
            throw new IllegalArgumentException("Invalid empty attributes");
        }

        Query baseQuery = em.createQuery(getBaseQueryStringWithCheck());
        List<List<Attribute<?, ?>>> attributeList = getAndCheckAttributes(attributes);
        TypedQuery<Object[]> exampleQuery = getExampleQuery(attributeList);
        String[] returningColumns = getReturningColumns(attributeList);
        return getExecuteWithReturningQuery(exampleQuery, baseQuery, returningColumns, new ReturningTupleObjectBuilder());
    }

    public <Z> ReturningResult<Z> executeWithReturning(String attribute, Class<Z> type) {
        return getWithReturningQuery(attribute, type).getSingleResult();
    }

    @SuppressWarnings("unchecked")
    public <Z> TypedQuery<ReturningResult<Z>> getWithReturningQuery(String attribute, Class<Z> type) {
        if (attribute == null) {
            throw new NullPointerException("attribute");
        }
        if (type == null) {
            throw new NullPointerException("type");
        }
        if (attribute.isEmpty()) {
            throw new IllegalArgumentException("Invalid empty attribute");
        }

        AttributePath attrPath = JpaUtils.getBasicAttributePath(getMetamodel(), entityType, attribute);

        if (!type.isAssignableFrom(attrPath.getAttributeClass())) {
            throw new IllegalArgumentException("The given expected field type is not of the expected type: " + attrPath.getAttributeClass().getName());
        }

        List<List<Attribute<?, ?>>> attributes = new ArrayList<List<Attribute<?, ?>>>();
        attributes.add(attrPath.getAttributes());

        Query baseQuery = em.createQuery(getBaseQueryStringWithCheck());
        TypedQuery<Object[]> exampleQuery = getExampleQuery(attributes);
        String[] returningColumns = getReturningColumns(attributes);
        return getExecuteWithReturningQuery(exampleQuery, baseQuery, returningColumns, null);
    }

    public <Z> ReturningResult<Z> executeWithReturning(ReturningObjectBuilder<Z> objectBuilder) {
        return getWithReturningQuery(objectBuilder).getSingleResult();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <Z> TypedQuery<ReturningResult<Z>> getWithReturningQuery(ReturningObjectBuilder<Z> objectBuilder) {
        returningAttributes.clear();
        objectBuilder.applyReturning(this);
        List<List<Attribute<?, ?>>> attributes = getAndCheckReturningAttributes();

        Query baseQuery = em.createQuery(getBaseQueryStringWithCheck());
        TypedQuery<Object[]> exampleQuery = getExampleQuery(attributes);
        String[] returningColumns = getReturningColumns(attributes);
        return getExecuteWithReturningQuery(exampleQuery, baseQuery, returningColumns, objectBuilder);
    }
    
    private <R> TypedQuery<ReturningResult<R>> getExecuteWithReturningQuery(TypedQuery<Object[]> exampleQuery, Query baseQuery, String[] returningColumns, ReturningObjectBuilder<R> objectBuilder) {
        Set<String> parameterListNames = parameterManager.getParameterListNames(baseQuery);
        boolean shouldRenderCteNodes = renderCteNodes(false);
        List<CTENode> ctes = shouldRenderCteNodes ? getCteNodes(baseQuery, false) : Collections.EMPTY_LIST;
        QuerySpecification querySpecification = new ReturningModificationQuerySpecification<R>(
                this, baseQuery, exampleQuery, parameterListNames, mainQuery.cteManager.isRecursive(), ctes, shouldRenderCteNodes, returningColumns, objectBuilder
        );

        CustomReturningSQLTypedQuery query = new CustomReturningSQLTypedQuery<R>(
                querySpecification,
                exampleQuery,
                parameterManager.getValuesParameters(),
                parameterManager.getValuesBinders()
        );

        query.setFirstResult(firstResult);
        query.setMaxResults(maxResults);
        parameterManager.parameterizeQuery(query);
        return query;
    }
    
    private List<List<Attribute<?, ?>>> getAndCheckReturningAttributes() {
        int attributeCount = returningAttributes.size();
        if (attributeCount == 0) {
            throw new IllegalArgumentException("Invalid empty attributes");
        }
        return new ArrayList<>(returningAttributes.values());
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void validateReturningAttributes() {
        int attributeCount = returningAttributeBindingMap.size();
        if (attributeCount == 0) {
            throw new IllegalArgumentException("Invalid empty attributes");
        }
        
        Set<Attribute<?, ?>> availableAttributes;
        if (cteType == null || (availableAttributes = ((EntityType) cteType).getAttributes()).size() == returningAttributeBindingMap.size()) {
            return;
        }
        
        Set<String> attributeNames = new TreeSet<String>();
        for (Attribute<?, ?> attr : availableAttributes) {
            String attributeName = attr.getName();
            if (!returningAttributeBindingMap.containsKey(attributeName)) {
                attributeNames.add(attributeName);
            }
        }
        
        throw new IllegalArgumentException("The following required CTE attributes are not bound: " + attributeNames);
    }

    @Override
    public SimpleReturningBuilder returning(String modificationQueryAttribute) {
        if (modificationQueryAttribute == null) {
            throw new NullPointerException("modificationQueryAttribute");
        }
        if (modificationQueryAttribute.isEmpty()) {
            throw new IllegalArgumentException("Invalid empty modificationQueryAttribute");
        }

        if (isReturningEntityAliasAllowed && modificationQueryAttribute.equals(entityAlias)) {
            // Our little special case, since there would be no other way to refer to the id as the object type
            Attribute<?, ?> idAttribute = JpaUtils.getIdAttribute(entityType);
            modificationQueryAttribute = idAttribute.getName();
        }

        List<Attribute<?, ?>> attributePath = JpaUtils.getBasicAttributePath(getMetamodel(), entityType, modificationQueryAttribute).getAttributes();
        if (returningAttributes.put(modificationQueryAttribute, attributePath) != null) {
            throw new IllegalArgumentException("The entity attribute [" + modificationQueryAttribute + "] has already been returned!");
        }

        return this;
    }

    @SuppressWarnings("unchecked")
    public X returning(String cteAttribute, String modificationQueryAttribute) {
        if (cteAttribute == null) {
            throw new NullPointerException("cteAttribute");
        }
        if (modificationQueryAttribute == null) {
            throw new NullPointerException("modificationQueryAttribute");
        }
        if (cteAttribute.isEmpty()) {
            throw new IllegalArgumentException("Invalid empty cteAttribute");
        }
        if (modificationQueryAttribute.isEmpty()) {
            throw new IllegalArgumentException("Invalid empty modificationQueryAttribute");
        }

        Map.Entry<AttributePath, String[]> attributeEntry = attributeColumnMappings.get(cteAttribute);

        if (attributeEntry == null) {
            if (cteType.getAttribute(cteAttribute) != null) {
                throw new IllegalArgumentException("Can't bind the embeddable cte attribute [" + cteAttribute + "] directly! Please bind the respective sub attributes.");
            }
            throw new IllegalArgumentException("The cte attribute [" + cteAttribute + "] does not exist!");
        }
        
        Class<?> queryAttrType;
        if (isReturningEntityAliasAllowed && modificationQueryAttribute.equals(entityAlias)) {
            // Our little special case, since there would be no other way to refer to the id as the object type
            queryAttrType = entityType.getJavaType();
            Attribute<?, ?> idAttribute = JpaUtils.getIdAttribute(entityType);
            modificationQueryAttribute = idAttribute.getName();
        } else {
            AttributePath queryAttributePath = JpaUtils.getBasicAttributePath(getMetamodel(), entityType, modificationQueryAttribute);
            queryAttrType = queryAttributePath.getAttributeClass();
        }

        AttributePath cteAttr = attributeEntry.getKey();
        Class<?> cteAttrType = cteAttr.getAttributeClass();
        // NOTE: Actually we would check if the dbms supports returning this kind of attribute,
        // but if it already supports the returning clause, it can only also support returning all columns
        if (!cteAttrType.isAssignableFrom(queryAttrType)) {
            throw new IllegalArgumentException("The given cte attribute '" + cteAttribute + "' with the type '" + cteAttrType.getName() + "'"
                + " can not be assigned with a value of the type '" + queryAttrType.getName() + "' of the query attribute '" + modificationQueryAttribute + "'!");
        }
        
        if (returningAttributeBindingMap.put(cteAttribute, modificationQueryAttribute) != null) {
            throw new IllegalArgumentException("The cte attribute [" + cteAttribute + "] has already been bound!");
        }
        for (String column : attributeEntry.getValue()) {
            if (columnBindingMap.put(column, cteAttribute) != null) {
                throw new IllegalArgumentException("The cte column [" + column + "] has already been bound!");
            }
        }

        return (X) this;
    }
    
    public Y end() {
        validateReturningAttributes();
        listener.onBuilderEnded(this);
        return result;
    }
    
    public CTEInfo createCTEInfo() {
        List<String> attributes = prepareAndGetAttributes();
        List<String> columns = prepareAndGetColumnNames();
        CTEInfo info = new CTEInfo(cteName, cteType, attributes, columns, false, false, this, null);
        return info;
    }
    
    private List<List<Attribute<?, ?>>> getAndCheckAttributes(String[] attributes) {
        List<List<Attribute<?, ?>>> attrs = new ArrayList<List<Attribute<?, ?>>>(attributes.length);

        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i] == null) {
                throw new NullPointerException("attribute at position " + i);
            }
            if (attributes[i].isEmpty()) {
                throw new IllegalArgumentException("empty attribute at position " + i);
            }
            
            attrs.add(JpaUtils.getBasicAttributePath(getMetamodel(), entityType, attributes[i]).getAttributes());
        }
        
        return attrs;
    }
    
    private String[] getReturningColumns(List<List<Attribute<?, ?>>> attributes) {
        List<String> columns = new ArrayList<String>(attributes.size());

        StringBuilder sb = new StringBuilder();
        for (List<Attribute<?, ?>> returningAttribute : attributes) {
            sb.append(returningAttribute.get(0).getName());
            for (int i = 1; i < returningAttribute.size(); i++) {
                sb.append('.').append(returningAttribute.get(i).getName());
            }
            for (String column : cbf.getExtendedQuerySupport().getColumnNames(em, entityType, sb.toString())) {
                columns.add(column);
            }
            sb.setLength(0);
        }
        
        return columns.toArray(new String[columns.size()]);
    }
    
    private String[] getReturningColumns() {
        if (returningAttributeBindingMap.isEmpty()) {
            return null;
        }
        
        Collection<String> returningAttributeNames = returningAttributeBindingMap.values();
        List<String> columns = new ArrayList<String>(returningAttributeNames.size());

        for (String returningAttributeName : returningAttributeBindingMap.values()) {
            for (String column : cbf.getExtendedQuerySupport().getColumnNames(em, entityType, returningAttributeName)) {
                columns.add(column);
            }
        }
        
        return columns.toArray(new String[columns.size()]);
    }

    private Query getCountExampleQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(e) FROM ");
        sb.append(entityType.getName());
        sb.append(" e");
        
        String exampleQueryString = sb.toString();
        return em.createQuery(exampleQueryString);
    }
    
    private TypedQuery<Object[]> getExampleQuery(List<List<Attribute<?, ?>>> attributes) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        
        boolean first = true;
        for (List<Attribute<?, ?>> attrPath : attributes) {
            Attribute<?, ?> lastPathElem = attrPath.get(attrPath.size() - 1);
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            
            // TODO: actually we should also check if the attribute is a @GeneratedValue
            if (!dbmsDialect.supportsReturningColumns() && !JpaUtils.getIdAttribute(entityType).equals(lastPathElem)) {
                throw new IllegalArgumentException("Returning the query attribute [" + lastPathElem.getName() + "] is not supported by the dbms, only generated keys can be returned!");
            }

            sb.append(entityAlias).append('.');
            if (JpaUtils.isJoinable(lastPathElem)) {
                // We have to map *-to-one relationships to their ids
                EntityType<?> type = mainQuery.metamodel.entity(JpaUtils.resolveFieldClass(entityType.getJavaType(), lastPathElem));
                Attribute<?, ?> idAttribute = JpaUtils.getIdAttribute(type);
                // NOTE: Since we are talking about *-to-ones, the expression can only be a path to an object
                // so it is safe to just append the id to the path
                sb.append(lastPathElem.getName()).append('.').append(idAttribute.getName());
            } else {
                sb.append(attrPath.get(0).getName());
                for (int i = 1; i < attrPath.size(); i++) {
                    sb.append('.').append(attrPath.get(i).getName());
                }
            }
        }
        
        sb.append(" FROM ");
        sb.append(entityType.getName()).append(' ').append(entityAlias);

        String exampleQueryString = sb.toString();
        return em.createQuery(exampleQueryString, Object[].class);
    }
    
    protected List<String> prepareAndGetAttributes() {
        return new ArrayList<String>(returningAttributeBindingMap.keySet());
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

}
