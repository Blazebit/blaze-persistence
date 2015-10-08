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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

import com.blazebit.persistence.BaseModificationCriteriaBuilder;
import com.blazebit.persistence.ReturningBuilder;
import com.blazebit.persistence.ReturningModificationCriteriaBuilderFactory;
import com.blazebit.persistence.ReturningObjectBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.SelectCTECriteriaBuilder;
import com.blazebit.persistence.SelectRecursiveCTECriteriaBuilder;
import com.blazebit.persistence.impl.builder.object.ReturningTupleObjectBuilder;
import com.blazebit.persistence.spi.DbmsDialect;

/**
 *
 * @param <T> The entity type of this modification builder 
 * @author Christian Beikov
 * @since 1.1.0
 */
public abstract class AbstractModificationCriteriaBuilder<T, X extends BaseModificationCriteriaBuilder<X>, Y> extends AbstractCommonQueryBuilder<T, X> implements BaseModificationCriteriaBuilder<X>, CTEInfoBuilder {

	protected final EntityType<T> entityType;
	protected final String entityAlias;
	protected final EntityType<?> cteType;
	protected final String cteName;
	protected final Y result;
	protected final CTEBuilderListener listener;
	protected final Map<String, String> returningAttributeBindingMap;

	@SuppressWarnings("unchecked")
	public AbstractModificationCriteriaBuilder(CriteriaBuilderFactoryImpl cbf, EntityManager em, DbmsDialect dbmsDialect, Class<T> clazz, String alias, Set<String> registeredFunctions, ParameterManager parameterManager, Class<?> cteClass, Y result, CTEBuilderListener listener) {
		// NOTE: using tuple here because this class is used for the join manager and tuple is definitively not an entity
		// but in case of the insert criteria, the appropriate return type which is convenient because update and delete don't have a return type
		super(cbf, em, dbmsDialect, (Class<T>) Tuple.class, null, registeredFunctions, parameterManager);
		this.entityType = em.getMetamodel().entity(clazz);
		this.entityAlias = alias;
		this.result = result;
		this.listener = listener;
		
		if (cteClass == null) {
		    this.cteType = null;
		    this.cteName = null;
	        this.returningAttributeBindingMap = new LinkedHashMap<String, String>(0);
		} else {
            this.cteType = em.getMetamodel().entity(cteClass);
            this.cteName = cteType.getName();
    		this.returningAttributeBindingMap = new LinkedHashMap<String, String>(cteType.getAttributes().size());
		}
	}

    @Override
    public <Z> SelectCTECriteriaBuilder<Z, X> with(Class<Z> cteClass) {
        if (!dbmsDialect.supportsWithClauseInModificationQuery()) {
            throw new UnsupportedOperationException("The database does not support a with clause in modification queries!");
        }
        
        return super.with(cteClass);
    }

    @Override
    public <Z> SelectRecursiveCTECriteriaBuilder<Z, X> withRecursive(Class<Z> cteClass) {
        if (!dbmsDialect.supportsWithClauseInModificationQuery()) {
            throw new UnsupportedOperationException("The database does not support a with clause in modification queries!");
        }
        
        return super.withRecursive(cteClass);
    }

    @Override
    public <Z> ReturningModificationCriteriaBuilderFactory<X> withReturning(Class<Z> cteClass) {
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
        Query query;
        
        if (hasLimit() || cteManager.hasCtes() || returningAttributeBindingMap.size() > 0) {
            // We need to change the underlying sql when doing a limit with hibernate since it does not support limiting insert ... select statements
            // For CTEs we will also need to change the underlying sql
            List<Query> participatingQueries = getParticipatingQueries();
            
            query = em.createQuery(getBaseQueryString());
            participatingQueries.add(query);
            
            StringBuilder sqlSb = new StringBuilder(cbf.getExtendedQuerySupport().getSql(em, query));
            applyCtes(sqlSb);
            applyLimit(sqlSb);
            applyReturning(sqlSb);
            String finalSql = sqlSb.toString();
            
            query = new CustomSQLQuery(participatingQueries, query, em, cbf.getExtendedQuerySupport(), finalSql);
        } else {
            query = em.createQuery(getBaseQueryString());
        }
        
        parameterizeQuery(query);
        return query;
	}
    
    protected void applyReturning(StringBuilder sqlSb) {
        if (returningAttributeBindingMap.isEmpty()) {
            return;
        }
        
        // Since for now PostgreSQL seems to be the only DBMS that supports
        // the returning clause, we will adapt the syntax and introduce a DbmsDialect method
        // as soon as the need arises
        sqlSb.append(" returning ");
        
        boolean first = true;
        for (String returningAttributeName : returningAttributeBindingMap.values()) {
            String[] columns = cbf.getExtendedQuerySupport().getColumnNames(em, entityType, returningAttributeName);
            for (String column : columns) {
                if (first) {
                    first = false;
                } else {
                    sqlSb.append(", ");
                }
                
                sqlSb.append(column);
            }
        }
    }
	
	protected Query getBaseQuery() {
	    Query query = em.createQuery(getBaseQueryString());
        parameterizeQuery(query);
        return query;
	}

	public int executeUpdate() {
		return getQuery().executeUpdate();
	}

	public ReturningResult<Tuple> executeWithReturning(String... attributes) {
	    if (attributes == null) {
	        throw new NullPointerException("attributes");
	    }
	    if (attributes.length == 0) {
	        throw new IllegalArgumentException("Invalid empty attributes");
	    }

        Query exampleQuery = getExampleQuery(attributes);
        Query baseQuery = getBaseQuery();
        final ReturningResult<Object[]> result = executeWithReturning(exampleQuery, baseQuery);
        final List<Object[]> originalResultList = result.getResultList();
        final int updateCount = result.getUpdateCount();
        return new DefaultReturningResult<Tuple>(originalResultList, updateCount, dbmsDialect, new ReturningTupleObjectBuilder());
	}

    @SuppressWarnings("unchecked")
    public <Z> ReturningResult<Z> executeWithReturning(String attribute, Class<Z> type) {
        Attribute<?, ?> attr = JpaUtils.getAttribute(entityType, attribute);
        if (!type.isAssignableFrom(attr.getJavaType())) {
            throw new IllegalArgumentException("The given expected field type is not of the expected type: " + attr.getJavaType().getName());
        }

        Query exampleQuery = getExampleQuery(new String[]{ attribute });
        Query baseQuery = getBaseQuery();
        final ReturningResult<Object[]> result = executeWithReturning(exampleQuery, baseQuery);
        final List<Object[]> originalResultList = result.getResultList();
        final int updateCount = result.getUpdateCount();
        
        // The single element case will not return object arrays
        return new DefaultReturningResult<Z>((List<Z>) originalResultList, updateCount, dbmsDialect);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
    public <Z> ReturningResult<Z> executeWithReturning(ReturningObjectBuilder<Z> objectBuilder) {
	    // TODO: this is not really nice, we should abstract that somehow
	    objectBuilder.applyReturning((ReturningBuilder) this);
	    String[] attributes = getAndCheckReturningAttributes();
	    returningAttributeBindingMap.clear();
	    
        Query exampleQuery = getExampleQuery(attributes);
        Query baseQuery = getBaseQuery();
        final ReturningResult<Object[]> result = executeWithReturning(exampleQuery, baseQuery);
        final List<Object[]> originalResultList = result.getResultList();
        final int updateCount = result.getUpdateCount();
        return new DefaultReturningResult<Z>(originalResultList, updateCount, dbmsDialect, objectBuilder);
	}
	
	private ReturningResult<Object[]> executeWithReturning(Query exampleQuery, Query baseQuery) {
        List<Query> participatingQueries = getParticipatingQueries();
        participatingQueries.add(baseQuery);
        
        StringBuilder sqlSb = new StringBuilder(cbf.getExtendedQuerySupport().getSql(em, baseQuery));
        applyCtes(sqlSb);
        applyLimit(sqlSb);
        String finalSql = sqlSb.toString();
        
        // TODO: hibernate will return the object directly for single attribute case instead of an object array
        final ReturningResult<Object[]> result = cbf.getExtendedQuerySupport().executeReturning(em, participatingQueries, exampleQuery, finalSql);
        return result;
	}
	
    private String[] getAndCheckReturningAttributes() {
        validateReturningAttributes();
        return returningAttributeBindingMap.keySet().toArray(new String[returningAttributeBindingMap.size()]);
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
        Attribute<?, ?> cteAttr = JpaUtils.getAttribute(cteType, cteAttribute);
        if (cteAttr == null) {
            throw new IllegalArgumentException("The cte attribute [" + cteAttribute + "] does not exist!");
        }
        
        Attribute<?, ?> queryAttr = JpaUtils.getAttribute(entityType, modificationQueryAttribute);
        Class<?> queryAttrType;
        if (queryAttr == null) {
            if (modificationQueryAttribute.equals(entityAlias)) {
                // Our little special case, since there would be no other way to refer to the id as the object type
                queryAttrType = entityType.getJavaType();
                Attribute<?, ?> idAttribute = JpaUtils.getIdAttribute(entityType);
                modificationQueryAttribute = idAttribute.getName();
            } else {
                throw new IllegalArgumentException("The query attribute [" + modificationQueryAttribute + "] does not exist!");
            }
        } else {
            queryAttrType = queryAttr.getJavaType();
        }
        
        if (!cteAttr.getJavaType().isAssignableFrom(queryAttrType)) {
            throw new IllegalArgumentException("The given cte attribute '" + cteAttribute + "' with the type '" + cteAttr.getJavaType().getName() + "'"
                + " can not be assigned with a value of the type '" + queryAttr.getJavaType().getName() + "' of the query attribute '" + modificationQueryAttribute + "'!");
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
        CTEInfo info = new CTEInfo(cteName, cteType, attributes, false, this, null);
        return info;
    }
    
    private Query getExampleQuery(String[] attributes) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        
        boolean first = true;
        for (int i = 0; i < attributes.length; i++) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            
            Attribute<?, ?> attribute = JpaUtils.getAttribute(entityType, attributes[i]);
            if (attribute == null) {
                if (attributes[i].equals(entityAlias)) {
                    // Our little special case, since there would be no other way to refer to the id as the object type
                    Attribute<?, ?> idAttribute = JpaUtils.getIdAttribute(entityType);
                    sb.append(idAttribute.getName());
                } else {
                    throw new IllegalArgumentException("The query attribute [" + attributes[i] + "] does not exist!");
                }
            }
            
            if (JpaUtils.isJoinable(attribute)) {
                // We have to map *-to-one relationships to their ids
                EntityType<?> type = em.getMetamodel().entity(JpaUtils.resolveFieldClass(entityType.getJavaType(), attribute));
                Attribute<?, ?> idAttribute = JpaUtils.getIdAttribute(type);
                // NOTE: Since we are talking about *-to-ones, the expression can only be a path to an object
                // so it is safe to just append the id to the path
                sb.append(attributes[i]).append('.').append(idAttribute.getName());
            } else {
                sb.append(attributes[i]);
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
