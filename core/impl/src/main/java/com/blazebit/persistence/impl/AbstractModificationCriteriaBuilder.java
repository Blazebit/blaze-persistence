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

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;

import com.blazebit.persistence.ModificationCriteriaBuilder;
import com.blazebit.persistence.spi.DbmsDialect;

/**
 *
 * @param <T> The entity type of this modification builder 
 * @author Christian Beikov
 * @since 1.1.0
 */
public class AbstractModificationCriteriaBuilder<T, X extends ModificationCriteriaBuilder<T, X>> extends AbstractCommonQueryBuilder<T, X> implements ModificationCriteriaBuilder<T, X> {

	protected final EntityType<T> entityType;
	protected final String entityAlias;

	public AbstractModificationCriteriaBuilder(CriteriaBuilderFactoryImpl cbf, EntityManager em, DbmsDialect dbmsDialect, Class<T> clazz, String alias, Set<String> registeredFunctions) {
		super(cbf, em, dbmsDialect, clazz, alias, registeredFunctions);
		this.entityType = em.getMetamodel().entity(clazz);
		this.entityAlias = alias;
	}

	@Override
	public Query getQuery() {
        Query query = em.createQuery(getQueryString());
        parameterizeQuery(query);
        return query;
	}

	@Override
	public int executeUpdate() {
		return getQuery().executeUpdate();
	}

}
