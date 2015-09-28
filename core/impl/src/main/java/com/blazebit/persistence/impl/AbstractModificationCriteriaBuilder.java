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
import javax.persistence.Tuple;
import javax.persistence.metamodel.EntityType;

import com.blazebit.persistence.BaseModificationCriteriaBuilder;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.spi.DbmsDialect;

/**
 *
 * @param <T> The entity type of this modification builder 
 * @author Christian Beikov
 * @since 1.1.0
 */
public abstract class AbstractModificationCriteriaBuilder<T, X extends BaseModificationCriteriaBuilder<X>, Y> extends AbstractCommonQueryBuilder<T, X> implements BaseModificationCriteriaBuilder<X>, CTEInfoBuilder {

	protected final EntityType<T> entityType;
	protected final String entityAlias;
	protected final Y result;
	protected final CTEBuilderListener listener;

	@SuppressWarnings("unchecked")
	public AbstractModificationCriteriaBuilder(CriteriaBuilderFactoryImpl cbf, EntityManager em, DbmsDialect dbmsDialect, Class<T> clazz, String alias, Set<String> registeredFunctions, Y result, CTEBuilderListener listener) {
		// NOTE: using tuple here because this class is used for the join manager and tuple is definitively not an entity
		// but in case of the insert criteria, the appropriate return type which is convenient because update and delete don't have a return type
		super(cbf, em, dbmsDialect, (Class<T>) Tuple.class, null, registeredFunctions);
		this.entityType = em.getMetamodel().entity(clazz);
		this.entityAlias = alias;
		this.result = result;
		this.listener = listener;
	}

	public Query getQuery() {
        Query query = em.createQuery(getQueryString());
        parameterizeQuery(query);
        return query;
	}

	public int executeUpdate() {
		return getQuery().executeUpdate();
	}

	public ReturningResult<Tuple> executeWithReturning(String... attributes) {
//		String sql = cbf.getExtendedQuerySupport().getSql(em, query);
		// TODO Auto-generated method stub
		return null;
	}

	public <Z> ReturningResult<Z> executeWithReturning(String attribute, Class<Z> type) {
		// TODO Auto-generated method stub
		return null;
	}

	public <Z> ReturningResult<Z> executeWithReturning(ObjectBuilder<Z> objectBuilder) {
		// TODO Auto-generated method stub
		return null;
	}

    public Y returning(String... attributes) {
        // TODO Auto-generated method stub
        return null;
    }

    public Y returning(String attribute, Class<?> type) {
        // TODO Auto-generated method stub
        return null;
    }

    public Y returning(ObjectBuilder<?> objectBuilder) {
        // TODO Auto-generated method stub
        return null;
    }
	
	protected void appendReturningClause(StringBuilder sbSelectFrom) {
		if (orderByManager.hasOrderBys()) {
			sbSelectFrom.append(", FUNCTION('RETURNING',");
		} else if (havingManager.hasPredicates()) {
			sbSelectFrom.append(" AND 1=FUNCTION('RETURNING',");
		} else if (groupByManager.hasGroupBys()) {
			sbSelectFrom.append(", FUNCTION('RETURNING',");
		} else if (whereManager.hasPredicates()) {
			sbSelectFrom.append(" AND 1=FUNCTION('RETURNING',");
		} else {
			sbSelectFrom.append(" WHERE 1=FUNCTION('RETURNING',");
		}

		sbSelectFrom.append(")");
	}

}
