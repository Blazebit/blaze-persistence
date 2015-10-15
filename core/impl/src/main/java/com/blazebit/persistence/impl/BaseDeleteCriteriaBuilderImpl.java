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

import com.blazebit.persistence.BaseDeleteCriteriaBuilder;
import com.blazebit.persistence.spi.DbmsStatementType;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public class BaseDeleteCriteriaBuilderImpl<T, X extends BaseDeleteCriteriaBuilder<T, X>, Y> extends AbstractModificationCriteriaBuilder<T, X, Y> implements BaseDeleteCriteriaBuilder<T, X> {

	public BaseDeleteCriteriaBuilderImpl(MainQuery mainQuery, boolean isMainQuery, Class<T> clazz, String alias, Class<?> cteClass, Y result, CTEBuilderListener listener) {
		super(mainQuery, isMainQuery, DbmsStatementType.DELETE, clazz, alias, cteClass, result, listener);

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
	protected void getQueryString1(StringBuilder sbSelectFrom) {
		sbSelectFrom.append("DELETE FROM ");
		sbSelectFrom.append(entityType.getName()).append(' ');
		sbSelectFrom.append(entityAlias);
    	appendWhereClause(sbSelectFrom);
	}

}
