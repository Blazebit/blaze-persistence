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

import java.util.List;
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
public class SelectCTECriteriaBuilderImpl<T, Y, X> extends AbstractCTECriteriaBuilder<T, Y, SelectCTECriteriaBuilder<T, Y>, SelectCTECriteriaBuilder<T, Y>> implements SelectCTECriteriaBuilder<T, Y> {

	public SelectCTECriteriaBuilderImpl(CriteriaBuilderFactoryImpl cbf, EntityManager em, DbmsDialect dbmsDialect, Class<T> clazz, Set<String> registeredFunctions, ParameterManager parameterManager, Y result, CTEBuilderListener listener) {
		super(cbf, em, dbmsDialect, clazz, registeredFunctions, parameterManager, result, listener);
	}

	@Override
	public Y end() {
		listener.onBuilderEnded(this);
		return result;
	}
	
	public CTEInfo createCTEInfo() {
		List<String> attributes = prepareAndGetAttributes();
		CTEInfo info = new CTEInfo(cteName, cteType, attributes, false, false, this, null);
		return info;
	}

}
