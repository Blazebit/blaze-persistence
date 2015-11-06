/*
 * Copyright 2015 Blazebit.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

import com.blazebit.persistence.BaseModificationCriteriaBuilder;
import com.blazebit.persistence.FullSelectCTECriteriaBuilder;
import com.blazebit.persistence.ReturningBuilder;
import com.blazebit.persistence.ReturningModificationCriteriaBuilderFactory;
import com.blazebit.persistence.ReturningObjectBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.SelectRecursiveCTECriteriaBuilder;
import com.blazebit.persistence.impl.builder.object.ReturningTupleObjectBuilder;
import com.blazebit.persistence.impl.dialect.DB2DbmsDialect;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.DbmsModificationState;
import com.blazebit.persistence.spi.DbmsStatementType;

/**
 *
 * @param <T> The entity type of this modification builder 
 * @author Christian Beikov
 * @since 1.1.0
 */
public abstract class AbstractModificationCriteriaBuilder<T, X extends BaseModificationCriteriaBuilder<X>, Y> extends AbstractCommonQueryBuilder<T, X, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, AbstractCommonQueryBuilder<?, ?, ?, ?, ?>, BaseFinalSetOperationBuilderImpl<T, ?, ?>> implements BaseModificationCriteriaBuilder<X>, CTEInfoBuilder {

	protected final EntityType<T> entityType;
	protected final String entityAlias;
	protected final EntityType<?> cteType;
	protected final String cteName;
	protected final Y result;
	protected final CTEBuilderListener listener;
	protected final boolean isReturningEntityAliasAllowed;
	protected final Map<String, String> returningAttributeBindingMap;

	@SuppressWarnings("unchecked")
	public AbstractModificationCriteriaBuilder(MainQuery mainQuery, boolean isMainQuery, DbmsStatementType statementType, Class<T> clazz, String alias, String cteName, Class<?> cteClass, Y result, CTEBuilderListener listener) {
		// NOTE: using tuple here because this class is used for the join manager and tuple is definitively not an entity
		// but in case of the insert criteria, the appropriate return type which is convenient because update and delete don't have a return type
		super(mainQuery, isMainQuery, statementType, (Class<T>) Tuple.class, null);

        // set defaults
        if (alias == null) {
            alias = clazz.getSimpleName().toLowerCase();
        } else {
            // If the user supplies an alias, the intention is clear
            fromClassExplicitelySet = true;
        }
        
		this.entityType = em.getMetamodel().entity(clazz);
		this.entityAlias = joinManager.addRoot(entityType, alias);
		this.result = result;
		this.listener = listener;
		
		if (cteClass == null) {
		    this.cteType = null;
		    this.cteName = null;
            this.isReturningEntityAliasAllowed = false;
	        this.returningAttributeBindingMap = new LinkedHashMap<String, String>(0);
		} else {
            this.cteType = em.getMetamodel().entity(cteClass);
            this.cteName = cteName;
            // Returning the "entity" is only allowed in CTEs
            this.isReturningEntityAliasAllowed = true;
    		this.returningAttributeBindingMap = new LinkedHashMap<String, String>(cteType.getAttributes().size());
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
        
        if (hasLimit() || cteManager.hasCtes() || returningAttributeBindingMap.size() > 0) {
            // We need to change the underlying sql when doing a limit with hibernate since it does not support limiting insert ... select statements
            // For CTEs we will also need to change the underlying sql
            List<Query> participatingQueries = new ArrayList<Query>();
            
            query = em.createQuery(getBaseQueryString());
            
            StringBuilder sqlSb = new StringBuilder(cbf.getExtendedQuerySupport().getSql(em, query));
            boolean isEmbedded = this instanceof ReturningBuilder;
            StringBuilder withClause = applyCtes(sqlSb, query, isEmbedded, participatingQueries);
            String[] returningColumns = getReturningColumns();
            // NOTE: CTEs will only be added, if this is a subquery
            Map<String, String> addedCtes = applyExtendedSql(sqlSb, false, isEmbedded, withClause, returningColumns, includedModificationStates);
            
            String finalSql = sqlSb.toString();
            participatingQueries.add(query);

            // Some dbms like DB2 will need to wrap modification queries in select queries when using CTEs
            if (cteManager.hasCtes() && returningAttributeBindingMap.isEmpty() && !dbmsDialect.usesExecuteUpdateWhenWithClauseInModificationQuery()) {
                query = getCountExampleQuery();
            }
            
            query = new CustomSQLQuery(participatingQueries, query, cbf, dbmsDialect, em, cbf.getExtendedQuerySupport(), finalSql, addedCtes);
        } else {
            query = em.createQuery(getBaseQueryString());
        }
        
        parameterizeQuery(query);
        return query;
	}
	
	protected Query getBaseQuery() {
	    Query query = em.createQuery(getBaseQueryString());
        parameterizeQuery(query);
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
	    if (attributes == null) {
	        throw new NullPointerException("attributes");
	    }
	    if (attributes.length == 0) {
	        throw new IllegalArgumentException("Invalid empty attributes");
	    }

	    List<List<Attribute<?, ?>>> attributeList = getAndCheckAttributes(attributes);
        Query exampleQuery = getExampleQuery(attributeList);
        Query baseQuery = getBaseQuery();
        String[] returningColumns = getReturningColumns(attributeList);
        final ReturningResult<Object[]> result = executeWithReturning(exampleQuery, baseQuery, returningColumns);
        final List<Object[]> originalResultList = result.getResultList();
        final int updateCount = result.getUpdateCount();
        return new DefaultReturningResult<Tuple>(originalResultList, updateCount, dbmsDialect, new ReturningTupleObjectBuilder());
	}

    @SuppressWarnings("unchecked")
    public <Z> ReturningResult<Z> executeWithReturning(String attribute, Class<Z> type) {
        if (attribute == null) {
            throw new NullPointerException("attribute");
        }
        if (type == null) {
            throw new NullPointerException("type");
        }
        if (attribute.isEmpty()) {
            throw new IllegalArgumentException("Invalid empty attribute");
        }
        
        List<Attribute<?,?>> attrPath = JpaUtils.getBasicAttributePath(getMetamodel(), entityType, attribute);
        
        if (!type.isAssignableFrom(attrPath.get(attrPath.size() - 1).getJavaType())) {
            throw new IllegalArgumentException("The given expected field type is not of the expected type: " + attrPath.get(attrPath.size() - 1).getJavaType().getName());
        }

        List<List<Attribute<?, ?>>> attributes = new ArrayList<List<Attribute<?, ?>>>();
        attributes.add(attrPath);
        
        Query exampleQuery = getExampleQuery(attributes);
        Query baseQuery = getBaseQuery();
        String[] returningColumns = getReturningColumns(attributes);
        final ReturningResult<Object[]> result = executeWithReturning(exampleQuery, baseQuery, returningColumns);
        final List<Object[]> originalResultList = result.getResultList();
        final int updateCount = result.getUpdateCount();
        
        // The single element case will not return object arrays
        return new DefaultReturningResult<Z>((List<Z>) originalResultList, updateCount, dbmsDialect);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
    public <Z> ReturningResult<Z> executeWithReturning(ReturningObjectBuilder<Z> objectBuilder) {
	    // TODO: this is not really nice, we should abstract that somehow
	    objectBuilder.applyReturning((ReturningBuilder) this);
	    List<List<Attribute<?, ?>>> attributes = getAndCheckReturningAttributes();
	    returningAttributeBindingMap.clear();
	    
        Query exampleQuery = getExampleQuery(attributes);
        Query baseQuery = getBaseQuery();
        String[] returningColumns = getReturningColumns(attributes);
        final ReturningResult<Object[]> result = executeWithReturning(exampleQuery, baseQuery, returningColumns);
        final List<Object[]> originalResultList = result.getResultList();
        final int updateCount = result.getUpdateCount();
        return new DefaultReturningResult<Z>(originalResultList, updateCount, dbmsDialect, objectBuilder);
	}
	
	private ReturningResult<Object[]> executeWithReturning(Query exampleQuery, Query baseQuery, String[] returningColumns) {
        List<Query> participatingQueries = new ArrayList<Query>();
        
        StringBuilder sqlSb = new StringBuilder(cbf.getExtendedQuerySupport().getSql(em, baseQuery));
        StringBuilder withClause = applyCtes(sqlSb, baseQuery, false, participatingQueries);
        applyExtendedSql(sqlSb, false, false, withClause, returningColumns, null);
        String finalSql = sqlSb.toString();
        participatingQueries.add(baseQuery);
        
        // TODO: hibernate will return the object directly for single attribute case instead of an object array
        final ReturningResult<Object[]> result = cbf.getExtendedQuerySupport().executeReturning(cbf, dbmsDialect, em, participatingQueries, exampleQuery, finalSql);
        return result;
	}
	
    private List<List<Attribute<?, ?>>> getAndCheckReturningAttributes() {
        validateReturningAttributes();
        return getAndCheckAttributes(returningAttributeBindingMap.keySet().toArray(new String[returningAttributeBindingMap.size()]));
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
        
        Attribute<?, ?> cteAttr = JpaUtils.getAttribute(cteType, cteAttribute);
        if (cteAttr == null) {
            throw new IllegalArgumentException("The cte attribute [" + cteAttribute + "] does not exist!");
        }
        
        List<Attribute<?, ?>> queryAttrs = JpaUtils.getBasicAttributePath(getMetamodel(), entityType, modificationQueryAttribute);
        Class<?> queryAttrType;
        if (queryAttrs.isEmpty()) {
            if (isReturningEntityAliasAllowed && modificationQueryAttribute.equals(entityAlias)) {
                // Our little special case, since there would be no other way to refer to the id as the object type
                queryAttrType = entityType.getJavaType();
                Attribute<?, ?> idAttribute = JpaUtils.getIdAttribute(entityType);
                modificationQueryAttribute = idAttribute.getName();
            } else {
                throw new IllegalArgumentException("The query attribute [" + modificationQueryAttribute + "] does not exist!");
            }
        } else {
            queryAttrType = queryAttrs.get(queryAttrs.size() - 1).getJavaType();
        }
        
        // NOTE: Actually we would check if the dbms supports returning this kind of attribute,
        // but if it already supports the returning clause, it can only also support returning all columns
        if (!cteAttr.getJavaType().isAssignableFrom(queryAttrType)) {
            throw new IllegalArgumentException("The given cte attribute '" + cteAttribute + "' with the type '" + cteAttr.getJavaType().getName() + "'"
                + " can not be assigned with a value of the type '" + queryAttrType.getName() + "' of the query attribute '" + modificationQueryAttribute + "'!");
        }
        
        String bindingEntry = returningAttributeBindingMap.get(cteAttribute);
        
        if (bindingEntry != null) {
            throw new IllegalArgumentException("The cte attribute [" + cteAttribute + "] has already been bound!");
        }
        
        returningAttributeBindingMap.put(cteAttribute, modificationQueryAttribute);
        return (X) this;
    }
    
    public Y end() {
        validateReturningAttributes();
        listener.onBuilderEnded(this);
        return result;
    }
    
    public CTEInfo createCTEInfo() {
        List<String> attributes = prepareAndGetAttributes();
        CTEInfo info = new CTEInfo(cteName, cteType, attributes, false, false, this, null);
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
            
            attrs.add(JpaUtils.getBasicAttributePath(getMetamodel(), entityType, attributes[i]));
        }
        
        return attrs;
    }
    
    private String[] getReturningColumns(List<List<Attribute<?, ?>>> attributes) {
        List<String> columns = new ArrayList<String>(attributes.size());

        StringBuilder sb = new StringBuilder();
        for (List<Attribute<?, ?>> returningAttribute : attributes) {
        	sb.append(returningAttribute.get(0).getName());
        	for(int i = 1; i < returningAttribute.size(); i++){
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
    
    private Query getExampleQuery(List<List<Attribute<?, ?>>> attributes) {
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
            
            if (JpaUtils.isJoinable(lastPathElem)) {
                // We have to map *-to-one relationships to their ids
                EntityType<?> type = em.getMetamodel().entity(JpaUtils.resolveFieldClass(entityType.getJavaType(), lastPathElem));
                Attribute<?, ?> idAttribute = JpaUtils.getIdAttribute(type);
                // NOTE: Since we are talking about *-to-ones, the expression can only be a path to an object
                // so it is safe to just append the id to the path
                sb.append(lastPathElem.getName()).append('.').append(idAttribute.getName());
            } else {
                sb.append(attrPath.get(0).getName());
                for(int i = 1; i < attrPath.size(); i++){
                	sb.append('.').append(attrPath.get(i).getName());
                }
            }
        }
        
        sb.append(" FROM ");
        sb.append(entityType.getName());
        
        String exampleQueryString = sb.toString();
        return em.createQuery(exampleQueryString);
    }
    
    protected List<String> prepareAndGetAttributes() {
        return new ArrayList<String>(returningAttributeBindingMap.keySet());
    }
    
    protected static class DefaultReturningResult<Z> implements ReturningResult<Z> {
        private final List<Z> resultList;
        private final int updateCount;
        private final DbmsDialect dbmsDialect;
        
        public DefaultReturningResult(List<Z> resultList, int updateCount, DbmsDialect dbmsDialect) {
            this.resultList = resultList;
            this.updateCount = updateCount;
            this.dbmsDialect = dbmsDialect;
        }

        public DefaultReturningResult(List<Object[]> originalResultList, int updateCount, DbmsDialect dbmsDialect, ReturningObjectBuilder<Z> objectBuilder) {
            this.updateCount = updateCount;
            this.dbmsDialect = dbmsDialect;
            final List<Z> resultList = new ArrayList<Z>(originalResultList.size());
            
            for (Object[] element : originalResultList) {
                resultList.add(objectBuilder.build(element));
            }
            
            this.resultList = objectBuilder.buildList(resultList);
        }

        @Override
        public Z getLastResult() {
            return resultList.get(resultList.size() - 1);
        }

        @Override
        public List<Z> getResultList() {
            if (dbmsDialect.supportsReturningAllGeneratedKeys()) {
                return resultList;
            }
            
            throw new UnsupportedOperationException("The database does not support returning all generated keys!");
        }

        @Override
        public int getUpdateCount() {
            return updateCount;
        }
    }
    
}
