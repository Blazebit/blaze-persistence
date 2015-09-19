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
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

import com.blazebit.persistence.CTECriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.SelectBuilder;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class CTECriteriaBuilderImpl<T, X> extends AbstractCommonQueryBuilder<T, CTECriteriaBuilder<T, X>> implements CTECriteriaBuilder<T, X>, SelectBuilder<CTECriteriaBuilder<T, X>> {
	
	private final CriteriaBuilder<X> result;
	private final CTEBuilderListener listener;
	private final String cteName;
	private final SortedMap<String, Boolean> bindingMap;
	
    public CTECriteriaBuilderImpl(CriteriaBuilderFactoryImpl cbf, EntityManager em, Class<T> clazz, Set<String> registeredFunctions, CriteriaBuilder<X> result, CTEBuilderListener listener) {
        super(cbf, em, clazz, null, registeredFunctions);
        this.result = result;
        this.listener = listener;

		EntityType<?> entityType = em.getMetamodel().entity(clazz);
		this.cteName = entityType.getName();
		this.bindingMap = new TreeMap<String, Boolean>();
		
		for (Attribute<?, ?> attribute : entityType.getAttributes()) {
			bindingMap.put(attribute.getName(), Boolean.FALSE);
		}
    }

	@Override
	public SelectBuilder<CTECriteriaBuilder<T, X>> bind(String cteAttribute) {
		Boolean attributeBound = bindingMap.get(cteAttribute);
		
		if (attributeBound == null) {
			throw new IllegalArgumentException("The cte attribute [" + cteAttribute + "] does not exist!");
		}
		if (attributeBound == Boolean.TRUE) {
			throw new IllegalArgumentException("The cte attribute [" + cteAttribute + "] has already been bound!");
		}
		
		bindingMap.put(cteAttribute, Boolean.TRUE);
		return this;
	}
	
	public String getCteName() {
		return cteName;
	}
	
	public List<String> getAttributes() {
		return new ArrayList<String>(bindingMap.keySet());
	}

	@Override
	public CriteriaBuilder<X> end() {
		listener.onBuilderEnded(this);
		return result;
	}

}
