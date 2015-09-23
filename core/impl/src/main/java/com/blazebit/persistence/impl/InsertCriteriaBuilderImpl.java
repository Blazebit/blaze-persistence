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

import com.blazebit.persistence.InsertCriteriaBuilder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.spi.DbmsDialect;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class InsertCriteriaBuilderImpl<T> extends AbstractModificationCriteriaBuilder<T, InsertCriteriaBuilder<T>> implements InsertCriteriaBuilder<T>, SelectBuilder<InsertCriteriaBuilder<T>> {

	
	private final Map<String, Integer> bindingMap = new TreeMap<String, Integer>();

	public InsertCriteriaBuilderImpl(CriteriaBuilderFactoryImpl cbf, EntityManager em, DbmsDialect dbmsDialect, Class<T> clazz, Set<String> registeredFunctions) {
		super(cbf, em, dbmsDialect, clazz, null, registeredFunctions);
	}

	@Override
	public InsertCriteriaBuilder<T> bind(String attributeName, Object value) {
		// Just do that to assert the attribute exists
		entityType.getAttribute(attributeName);
		bindingMap.put(attributeName, selectManager.getSelectInfos().size());
		selectManager.select(new ParameterExpression(attributeName), null);
		parameterManager.addParameterMapping(attributeName, value);
		return this;
	}

	@Override
	public SelectBuilder<InsertCriteriaBuilder<T>> bind(String attributeName) {
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
	protected void getQueryString1(StringBuilder sbSelectFrom) {
		sbSelectFrom.append("INSERT INTO ");
		sbSelectFrom.append(entityType.getName()).append('(');

		List<String> attributes = new ArrayList<String>(bindingMap.size());
		List<SelectInfo> originalSelectInfos = new ArrayList<SelectInfo>(selectManager.getSelectInfos());
		List<SelectInfo> newSelectInfos = selectManager.getSelectInfos();
		newSelectInfos.clear();
		
		boolean first = true;
		for (Map.Entry<String, Integer> attributeEntry : bindingMap.entrySet()) {
			// Reorder select infos to fit the attribute order
			Integer newPosition = attributes.size();
			attributes.add(attributeEntry.getKey());
			newSelectInfos.add(originalSelectInfos.get(attributeEntry.getValue()));
			attributeEntry.setValue(newPosition);
			
			if (first) {
				first = false;
			} else {
				sbSelectFrom.append(", ");
			}
			sbSelectFrom.append(attributeEntry.getKey());
		}
		
		sbSelectFrom.append(")\n");
    	super.getQueryString1(sbSelectFrom);
//    	appendReturningClause(sbSelectFrom);
	}

}
