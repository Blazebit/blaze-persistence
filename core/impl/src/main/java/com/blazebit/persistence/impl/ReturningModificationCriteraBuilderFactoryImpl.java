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

import com.blazebit.persistence.ReturningDeleteCriteriaBuilder;
import com.blazebit.persistence.ReturningInsertCriteriaBuilder;
import com.blazebit.persistence.ReturningModificationCriteriaBuilderFactory;
import com.blazebit.persistence.ReturningUpdateCriteriaBuilder;
import com.blazebit.persistence.spi.DbmsDialect;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.1.0
 */
public class ReturningModificationCriteraBuilderFactoryImpl<X> implements ReturningModificationCriteriaBuilderFactory<X> {

	private final CriteriaBuilderFactoryImpl cbf;
	private final EntityManager em;
	private final DbmsDialect dbmsDialect;
	private final Set<String> registeredFunctions;
	private final X result;
    private final CTEBuilderListener listener;

    ReturningModificationCriteraBuilderFactoryImpl(CriteriaBuilderFactoryImpl cbf, EntityManager em, DbmsDialect dbmsDialect, Set<String> registeredFunctions, X result, final CTEBuilderListener listener) {
    	this.cbf = cbf;
    	this.em = em;
    	this.dbmsDialect = dbmsDialect;
    	this.registeredFunctions = registeredFunctions;
    	this.result = result;
    	this.listener = listener;
    }

    @Override
    public <T> ReturningDeleteCriteriaBuilder<T, X> delete(Class<T> deleteClass) {
        return delete(deleteClass, null);
    }

    @Override
    public <T> ReturningDeleteCriteriaBuilder<T, X> delete(Class<T> deleteClass, String alias) {
        ReturningDeleteCriteriaBuilderImpl<T, X> cb = new ReturningDeleteCriteriaBuilderImpl<T, X>(cbf, em, dbmsDialect, deleteClass, alias, registeredFunctions, result, listener);
        listener.onBuilderStarted(cb);
        return cb;
    }

    @Override
    public <T> ReturningUpdateCriteriaBuilder<T, X> update(Class<T> updateClass) {
        return update(updateClass, null);
    }

    @Override
    public <T> ReturningUpdateCriteriaBuilder<T, X> update(Class<T> updateClass, String alias) {
        ReturningUpdateCriteriaBuilderImpl<T, X> cb = new ReturningUpdateCriteriaBuilderImpl<T, X>(cbf, em, dbmsDialect, updateClass, alias, registeredFunctions, result, listener);
        listener.onBuilderStarted(cb);
        return cb;
    }

    @Override
    public <T> ReturningInsertCriteriaBuilder<T, X> insert(Class<T> insertClass) {
        ReturningInsertCriteriaBuilderImpl<T, X> cb = new ReturningInsertCriteriaBuilderImpl<T, X>(cbf, em, dbmsDialect, insertClass, registeredFunctions, result, listener);
        listener.onBuilderStarted(cb);
        return cb;
    }

}
