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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.blazebit.persistence.BaseInsertCriteriaBuilder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.DbmsStatementType;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class BaseInsertCriteriaBuilderImpl<T, X extends BaseInsertCriteriaBuilder<T, X>, Y> extends AbstractModificationCriteriaBuilder<T, X, Y> implements BaseInsertCriteriaBuilder<T, X>, SelectBuilder<X> {

	private final Map<String, Integer> bindingMap = new TreeMap<String, Integer>();

	public BaseInsertCriteriaBuilderImpl(CriteriaBuilderFactoryImpl cbf, EntityManager em, DbmsDialect dbmsDialect, Class<T> clazz, Set<String> registeredFunctions, ParameterManager parameterManager, Class<?> cteClass, Y result, CTEBuilderListener listener) {
		super(cbf, em, DbmsStatementType.INSERT, dbmsDialect, clazz, null, registeredFunctions, parameterManager, cteClass, result, listener);
		
        if (!jpaProvider.supportsInsertStatement()) {
            throw new IllegalStateException("JPA provider does not support insert statements!");
        }
	}

    @Override
    @SuppressWarnings("unchecked")
	public X bind(String attributeName, Object value) {
		// Just do that to assert the attribute exists
		entityType.getAttribute(attributeName);
        Integer attributeBindIndex = bindingMap.get(attributeName);
        
        if (attributeBindIndex != null) {
            throw new IllegalArgumentException("The attribute [" + attributeName + "] has already been bound!");
        }
        
		bindingMap.put(attributeName, selectManager.getSelectInfos().size());
		String paramName = parameterManager.getParamNameForObject(value);
		selectManager.select(new ParameterExpression(paramName), null);
		
		return (X) this;
	}

    @Override
	public SelectBuilder<X> bind(String attributeName) {
		// Just do that to assert the attribute exists
		entityType.getAttribute(attributeName);
		Integer attributeBindIndex = bindingMap.get(attributeName);
		
		if (attributeBindIndex != null) {
			throw new IllegalArgumentException("The attribute [" + attributeName + "] has already been bound!");
		}
		
		bindingMap.put(attributeName, selectManager.getSelectInfos().size());
		return this;
	}
    
    @Override
    protected void prepareAndCheck() {
        List<String> attributes = new ArrayList<String>(bindingMap.size());
        List<SelectInfo> originalSelectInfos = new ArrayList<SelectInfo>(selectManager.getSelectInfos());
        List<SelectInfo> newSelectInfos = selectManager.getSelectInfos();
        newSelectInfos.clear();
        
        for (Map.Entry<String, Integer> attributeEntry : bindingMap.entrySet()) {
            // Reorder select infos to fit the attribute order
            Integer newPosition = attributes.size();
            attributes.add(attributeEntry.getKey());
            
            SelectInfo selectInfo = originalSelectInfos.get(attributeEntry.getValue());
            newSelectInfos.add(selectInfo);
            attributeEntry.setValue(newPosition);
        }
        super.prepareAndCheck();
    }

	@Override
    protected boolean isJoinRequiredForSelect() {
	    // NOTE: since we aren't actually selecting properties but passing them through to the insert, we don't require joins
        return false;
    }

    @Override
	protected void getQueryString1(StringBuilder sbSelectFrom) {
		sbSelectFrom.append("INSERT INTO ");
		sbSelectFrom.append(entityType.getName()).append('(');
		
		boolean first = true;
		for (Map.Entry<String, Integer> attributeEntry : bindingMap.entrySet()) {
			if (first) {
				first = false;
			} else {
				sbSelectFrom.append(", ");
			}
			
			sbSelectFrom.append(attributeEntry.getKey());
		}
		
		sbSelectFrom.append(")\n");
    	super.getQueryString1(sbSelectFrom);
	}

    @Override
    public Query getQuery() {
        if (jpaProvider.supportsInsertStatement()) {
            return super.getQuery();
        } else {
            // TODO: implement
            throw new UnsupportedOperationException("Not yet implemented!");
        }
    }

}
