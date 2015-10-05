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

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import com.blazebit.persistence.BaseUpdateCriteriaBuilder;
import com.blazebit.persistence.spi.DbmsDialect;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class BaseUpdateCriteriaBuilderImpl<T, X extends BaseUpdateCriteriaBuilder<T, X>, Y> extends AbstractModificationCriteriaBuilder<T, X, Y> implements BaseUpdateCriteriaBuilder<T, X> {

	private final Set<String> setAttributes = new LinkedHashSet<String>();

	public BaseUpdateCriteriaBuilderImpl(CriteriaBuilderFactoryImpl cbf, EntityManager em, DbmsDialect dbmsDialect, Class<T> clazz, String alias, Set<String> registeredFunctions, Class<?> cteClass, Y result, CTEBuilderListener listener) {
		super(cbf, em, dbmsDialect, clazz, alias, registeredFunctions, cteClass, result, listener);

        // set defaults
        if (alias == null) {
        	alias = clazz.getSimpleName().toLowerCase();
        } else {
        	// If the user supplies an alias, the intention is clear
        	fromClassExplicitelySet = true;
        }
        
        try {
            this.joinManager.addRoot(em.getMetamodel().entity(clazz), alias);
        } catch (IllegalArgumentException ex) {
    		throw new IllegalArgumentException("The class [" + clazz.getName() + "] is not an entity!");
        }
	}

    @Override
    @SuppressWarnings("unchecked")
	public X set(String attributeName, Object value) {
		// Just do that to assert the attribute exists
		entityType.getAttribute(attributeName);
		setAttributes.add(attributeName);
		parameterManager.addParameterMapping(attributeName, value);
		return (X) this;
	}

	@Override
	protected void getQueryString1(StringBuilder sbSelectFrom) {
		sbSelectFrom.append("UPDATE ");
		sbSelectFrom.append(entityType.getName()).append(' ');
		sbSelectFrom.append(entityAlias);
		sbSelectFrom.append(" SET ");
		
		for (String attribute : setAttributes) {
			sbSelectFrom.append(attribute);
			sbSelectFrom.append(" = :");
			sbSelectFrom.append(attribute);
		}
		
    	appendWhereClause(sbSelectFrom);
	}

}
