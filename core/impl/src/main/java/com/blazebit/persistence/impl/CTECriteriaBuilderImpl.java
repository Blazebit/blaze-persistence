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

import javax.persistence.EntityManager;

import com.blazebit.persistence.SelectCTECriteriaBuilder;
import com.blazebit.persistence.spi.DbmsDialect;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class CTECriteriaBuilderImpl<T, Y, X> extends AbstractCTECriteriaBuilder<T, Y, SelectCTECriteriaBuilder<T, Y>> implements SelectCTECriteriaBuilder<T, Y> {

	public CTECriteriaBuilderImpl(CriteriaBuilderFactoryImpl cbf, EntityManager em, DbmsDialect dbmsDialect, Class<T> clazz, Set<String> registeredFunctions, Y result, CTEBuilderListener listener) {
		super(cbf, em, dbmsDialect, clazz, registeredFunctions, result, listener);
	}

	@Override
	public Y end() {
		listener.onBuilderEnded(this);
		return result;
	}
	
	public CTEInfo createCTEInfo() {
		List<String> attributes = new ArrayList<String>(bindingMap.size());
		List<SelectInfo> originalSelectInfos = new ArrayList<SelectInfo>(selectManager.getSelectInfos());
		List<SelectInfo> newSelectInfos = selectManager.getSelectInfos();
		newSelectInfos.clear();
		
		for (Map.Entry<String, Integer> bindingEntry : bindingMap.entrySet()) {
			Integer newPosition = attributes.size();
			attributes.add(bindingEntry.getKey());
			newSelectInfos.add(originalSelectInfos.get(bindingEntry.getValue()));
			bindingEntry.setValue(newPosition);
		}
		
		CTEInfo info = new CTEInfo(cteName, attributes, false, this, null);
		return info;
	}

}
