/*
 * Copyright 2014 Blazebit.
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
import javax.persistence.TypedQuery;

import com.blazebit.persistence.CTECriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.RecursiveCTECriteriaBuilder;
import com.blazebit.persistence.SelectObjectBuilder;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class CriteriaBuilderImpl<T> extends AbstractQueryBuilder<T, CriteriaBuilder<T>> implements CriteriaBuilder<T> {
	
	private final CTEManager<T> cteManager;

    public CriteriaBuilderImpl(CriteriaBuilderFactoryImpl cbf, EntityManager em, Class<T> clazz, String alias, Set<String> registeredFunctions) {
        super(cbf, em, clazz, alias, registeredFunctions);
        this.cteManager = new CTEManager<T>(cbf, em, registeredFunctions);
    }

    @Override
    public CriteriaBuilder<T> from(Class<?> clazz) {
        return super.from(clazz);
    }

    @Override
    public CriteriaBuilder<T> from(Class<?> clazz, String alias) {
        return super.from(clazz, alias);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y> SelectObjectBuilder<CriteriaBuilder<Y>> selectNew(Class<Y> clazz) {
        return (SelectObjectBuilder<CriteriaBuilder<Y>>) super.selectNew(clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y> CriteriaBuilder<Y> selectNew(ObjectBuilder<Y> builder) {
        return (CriteriaBuilder<Y>) super.selectNew(builder);
    }
    
    public <Y> CTECriteriaBuilder<Y, T> with(Class<Y> cteClass) {
    	return cteManager.with(cteClass, this);
    }

    public <Y> RecursiveCTECriteriaBuilder<Y, T> withRecursive(Class<Y> cteClass) {
    	return cteManager.withRecursive(cteClass, this);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public TypedQuery<T> getQuery() {
        TypedQuery<T> query = (TypedQuery<T>) em.createQuery(getQueryString(), selectManager.getExpectedQueryResultType());
        if (selectManager.getSelectObjectBuilder() != null) {
            query = transformQuery(query);
        }

        parameterizeQuery(query);
        return query;
    }

    @Override
    public String getQueryString() {
        prepareAndCheck();
        return getQueryString0();
    }

    @Override
    protected String getQueryString0() {
        if (cachedQueryString == null) {
            cachedQueryString = getQueryString1();
        }

        return cachedQueryString;
    }

    @Override
    protected String getQueryString1() {
        StringBuilder sbSelectFrom = new StringBuilder();
        cteManager.buildClause(sbSelectFrom);
        getQueryString1(sbSelectFrom);
        return sbSelectFrom.toString();
    }

}
