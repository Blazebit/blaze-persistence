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

import java.util.LinkedHashMap;
import java.util.Map;

import com.blazebit.persistence.BaseUpdateCriteriaBuilder;
import com.blazebit.persistence.spi.DbmsStatementType;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class BaseUpdateCriteriaBuilderImpl<T, X extends BaseUpdateCriteriaBuilder<T, X>, Y> extends AbstractModificationCriteriaBuilder<T, X, Y> implements BaseUpdateCriteriaBuilder<T, X> {

	private final Map<String, String> setAttributes = new LinkedHashMap<String, String>();

	public BaseUpdateCriteriaBuilderImpl(MainQuery mainQuery, boolean isMainQuery, Class<T> clazz, String alias, Class<?> cteClass, Y result, CTEBuilderListener listener) {
		super(mainQuery, isMainQuery, DbmsStatementType.UPDATE, clazz, alias, cteClass, result, listener);
	}

    @Override
    @SuppressWarnings("unchecked")
	public X set(String attributeName, Object value) {
		// Just do that to assert the attribute exists
		entityType.getAttribute(attributeName);
        String attributeValue = setAttributes.get(attributeName);
        
        if (attributeValue != null) {
            throw new IllegalArgumentException("The attribute [" + attributeName + "] has already been bound!");
        }
        
		String paramName = parameterManager.getParamNameForObject(value);
		setAttributes.put(attributeName, paramName);
		return (X) this;
	}

	@Override
	protected void getQueryString1(StringBuilder sbSelectFrom) {
		sbSelectFrom.append("UPDATE ");
		sbSelectFrom.append(entityType.getName()).append(' ');
	    sbSelectFrom.append(entityAlias);
		sbSelectFrom.append(" SET ");
		
		for (Map.Entry<String, String> attributeEntry : setAttributes.entrySet()) {
			sbSelectFrom.append(attributeEntry.getKey());
			sbSelectFrom.append(" = :");
			sbSelectFrom.append(attributeEntry.getValue());
		}
		
    	appendWhereClause(sbSelectFrom);
	}

}
