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

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

import com.blazebit.persistence.BaseCTECriteriaBuilder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.spi.DbmsDialect;

/**
 *
 * @param <T> The query result type
 * @param <T> The criteria builder returned after the cte builder
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.1.0
 */
public abstract class AbstractCTECriteriaBuilder<T, Y, X extends BaseCTECriteriaBuilder<X>> extends AbstractCommonQueryBuilder<T, X> implements BaseCTECriteriaBuilder<X>, SelectBuilder<X> {
	
	protected static final Integer EMPTY = Integer.valueOf(-1);
	protected final Y result;
	protected final CTEBuilderListener listener;
	protected final String cteName;
	protected final SortedMap<String, Integer> bindingMap;
	
    public AbstractCTECriteriaBuilder(CriteriaBuilderFactoryImpl cbf, EntityManager em, DbmsDialect dbmsDialect, Class<T> clazz, Set<String> registeredFunctions, Y result, CTEBuilderListener listener) {
        super(cbf, em, dbmsDialect, clazz, null, registeredFunctions);
        this.result = result;
        this.listener = listener;

		EntityType<?> entityType = em.getMetamodel().entity(clazz);
		this.cteName = entityType.getName();
		this.bindingMap = new TreeMap<String, Integer>();
		
		for (Attribute<?, ?> attribute : entityType.getAttributes()) {
			bindingMap.put(attribute.getName(), EMPTY);
		}
    }

	public SelectBuilder<X> bind(String cteAttribute) {
		Integer attributeBindIndex = bindingMap.get(cteAttribute);
		
		if (attributeBindIndex == null) {
			throw new IllegalArgumentException("The cte attribute [" + cteAttribute + "] does not exist!");
		}
		if (attributeBindIndex != EMPTY) {
			throw new IllegalArgumentException("The cte attribute [" + cteAttribute + "] has already been bound!");
		}
		
		bindingMap.put(cteAttribute, selectManager.getSelectInfos().size());
		return this;
	}

	public abstract CTEInfo createCTEInfo();

}
