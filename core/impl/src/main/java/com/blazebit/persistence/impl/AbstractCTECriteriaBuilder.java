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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

import com.blazebit.persistence.BaseCTECriteriaBuilder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PropertyExpression;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.DbmsStatementType;

/**
 *
 * @param <T> The query result type
 * @param <T> The criteria builder returned after the cte builder
 * @param <X> The concrete builder type
 * @param <Z> The builder type that should be returned on set operations
 * @author Christian Beikov
 * @since 1.1.0
 */
public abstract class AbstractCTECriteriaBuilder<T, Y, X extends BaseCTECriteriaBuilder<X>, Z> extends AbstractCommonQueryBuilder<T, X, Z> implements BaseCTECriteriaBuilder<X>, SelectBuilder<X>, CTEInfoBuilder {
	
	protected static final Integer EMPTY = Integer.valueOf(-1);
	protected final Y result;
	protected final CTEBuilderListener listener;
	protected final String cteName;
	protected final EntityType<?> cteType;
	protected final Map<String, Integer> bindingMap;
	
    public AbstractCTECriteriaBuilder(CriteriaBuilderFactoryImpl cbf, EntityManager em, DbmsDialect dbmsDialect, Class<T> clazz, Set<String> registeredFunctions, ParameterManager parameterManager, Y result, CTEBuilderListener listener) {
        super(cbf, em, DbmsStatementType.SELECT, dbmsDialect, clazz, null, registeredFunctions, parameterManager);
        this.result = result;
        this.listener = listener;

		this.cteType = em.getMetamodel().entity(clazz);
		this.cteName = cteType.getName();
		this.bindingMap = new LinkedHashMap<String, Integer>();
    }

    @Override
    protected void getQueryString1(StringBuilder sbSelectFrom) {
        super.getQueryString1(sbSelectFrom);
        applyJpaLimit(sbSelectFrom);
    }

	@Override
    protected Query getQuery() {
        Query query;
        
        if (hasLimit()) {
            // We need to change the underlying sql when doing a limit
            query = em.createQuery(getBaseQueryString());
            List<Query> participatingQueries = Arrays.asList(query);
            
            StringBuilder sqlSb = new StringBuilder(cbf.getExtendedQuerySupport().getSql(em, query));
            applyExtendedSql(sqlSb, true, null, null, null);
            String finalSql = sqlSb.toString();
            
            query = new CustomSQLQuery(participatingQueries, query, dbmsDialect, em, cbf.getExtendedQuerySupport(), finalSql, null);
        } else {
            query = em.createQuery(getBaseQueryString());
        }
        
        parameterizeQuery(query);
        return query;
    }

    public SelectBuilder<X> bind(String cteAttribute) {
		Attribute<?, ?> attribute = cteType.getAttribute(cteAttribute);
		
		if (attribute == null) {
			throw new IllegalArgumentException("The cte attribute [" + cteAttribute + "] does not exist!");
		}
		if (bindingMap.containsKey(cteAttribute)) {
			throw new IllegalArgumentException("The cte attribute [" + cteAttribute + "] has already been bound!");
		}
		
		bindingMap.put(cteAttribute, selectManager.getSelectInfos().size());
		return this;
	}
    
    protected List<String> prepareAndGetAttributes() {
        List<String> attributes = new ArrayList<String>(bindingMap.size());
        for (Map.Entry<String, Integer> bindingEntry : bindingMap.entrySet()) {
            final String attributeName = bindingEntry.getKey();
            Attribute<?, ?> attribute = cteType.getAttribute(attributeName);
            attributes.add(attributeName);
            
            if (JpaUtils.isJoinable(attribute)) {
                // We have to map *-to-one relationships to their ids
                EntityType<?> type = em.getMetamodel().entity(JpaUtils.resolveFieldClass(cteType.getJavaType(), attribute));
                Attribute<?, ?> idAttribute = JpaUtils.getIdAttribute(type);
                // NOTE: Since we are talking about *-to-ones, the expression can only be a path to an object
                // so it is safe to just append the id to the path
                PathExpression pathExpression = (PathExpression) selectManager.getSelectInfos().get(bindingEntry.getValue()).getExpression();
                pathExpression.getExpressions().add(new PropertyExpression(idAttribute.getName()));
            }
        }
        
//        List<SelectInfo> originalSelectInfos = new ArrayList<SelectInfo>(selectManager.getSelectInfos());
//        List<SelectInfo> newSelectInfos = selectManager.getSelectInfos();
//        newSelectInfos.clear();
//        
//        for (Map.Entry<String, Integer> bindingEntry : bindingMap.entrySet()) {
//            Integer newPosition = attributes.size();
//            SelectInfo selectInfo = originalSelectInfos.get(bindingEntry.getValue());
//            
//            newSelectInfos.add(selectInfo);
//            bindingEntry.setValue(newPosition);
//        }
        
        return attributes;
    }

}
