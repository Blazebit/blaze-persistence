/*
 * Copyright 2014 - 2018 Blazebit.
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

import com.blazebit.persistence.ReturningDeleteCriteriaBuilder;
import com.blazebit.persistence.ReturningInsertCriteriaBuilder;
import com.blazebit.persistence.ReturningModificationCriteriaBuilderFactory;
import com.blazebit.persistence.ReturningUpdateCriteriaBuilder;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.1.0
 */
public class ReturningModificationCriteraBuilderFactoryImpl<X> implements ReturningModificationCriteriaBuilderFactory<X> {

    private final MainQuery mainQuery;
    private final String cteName;
    private final Class<?> cteClass;
    private final X result;
    private final CTEBuilderListener listener;

    ReturningModificationCriteraBuilderFactoryImpl(MainQuery mainQuery, String cteName, Class<?> cteClass, X result, final CTEBuilderListener listener) {
        this.mainQuery = mainQuery;
        this.cteName = cteName;
        this.cteClass = cteClass;
        this.result = result;
        this.listener = listener;
    }

    @Override
    public <T> ReturningDeleteCriteriaBuilder<T, X> delete(Class<T> deleteClass) {
        return delete(deleteClass, null);
    }

    @Override
    public <T> ReturningDeleteCriteriaBuilder<T, X> delete(Class<T> deleteClass, String alias) {
        ReturningDeleteCriteriaBuilderImpl<T, X> cb = new ReturningDeleteCriteriaBuilderImpl<T, X>(mainQuery, mainQuery.cteManager.getQueryContext(), deleteClass, alias, cteName, cteClass, result, listener);
        listener.onBuilderStarted(cb);
        return cb;
    }

    @Override
    public <T> ReturningDeleteCriteriaBuilder<T, X> deleteCollection(Class<T> deleteOwnerClass, String collectionName) {
        return deleteCollection(deleteOwnerClass, null, collectionName);
    }

    @Override
    public <T> ReturningDeleteCriteriaBuilder<T, X> deleteCollection(Class<T> deleteOwnerClass, String alias, String collectionName) {
        ReturningDeleteCollectionCriteriaBuilderImpl<T, X> cb = new ReturningDeleteCollectionCriteriaBuilderImpl<T, X>(mainQuery, mainQuery.cteManager.getQueryContext(), deleteOwnerClass, alias, cteName, cteClass, result, listener, collectionName);
        listener.onBuilderStarted(cb);
        return cb;
    }

    @Override
    public <T> ReturningUpdateCriteriaBuilder<T, X> update(Class<T> updateClass) {
        return update(updateClass, null);
    }

    @Override
    public <T> ReturningUpdateCriteriaBuilder<T, X> update(Class<T> updateClass, String alias) {
        ReturningUpdateCriteriaBuilderImpl<T, X> cb = new ReturningUpdateCriteriaBuilderImpl<T, X>(mainQuery, mainQuery.cteManager.getQueryContext(), updateClass, alias, cteName, cteClass, result, listener);
        listener.onBuilderStarted(cb);
        return cb;
    }

    @Override
    public <T> ReturningUpdateCriteriaBuilder<T, X> updateCollection(Class<T> updateOwnerClass, String collectionName) {
        return updateCollection(updateOwnerClass, null, collectionName);
    }

    @Override
    public <T> ReturningUpdateCriteriaBuilder<T, X> updateCollection(Class<T> updateOwnerClass, String alias, String collectionName) {
        ReturningUpdateCollectionCriteriaBuilderImpl<T, X> cb = new ReturningUpdateCollectionCriteriaBuilderImpl<T, X>(mainQuery, mainQuery.cteManager.getQueryContext(), updateOwnerClass, alias, cteName, cteClass, result, listener, collectionName);
        listener.onBuilderStarted(cb);
        return cb;
    }

    @Override
    public <T> ReturningInsertCriteriaBuilder<T, X> insert(Class<T> insertClass) {
        ReturningInsertCriteriaBuilderImpl<T, X> cb = new ReturningInsertCriteriaBuilderImpl<T, X>(mainQuery, mainQuery.cteManager.getQueryContext(), insertClass, cteName, cteClass, result, listener);
        listener.onBuilderStarted(cb);
        return cb;
    }

    @Override
    public <T> ReturningInsertCriteriaBuilder<T, X> insertCollection(Class<T> insertOwnerClass, String collectionName) {
        ReturningInsertCollectionCriteriaBuilderImpl<T, X> cb = new ReturningInsertCollectionCriteriaBuilderImpl<T, X>(mainQuery, mainQuery.cteManager.getQueryContext(), insertOwnerClass, cteName, cteClass, result, listener, collectionName);
        listener.onBuilderStarted(cb);
        return cb;
    }
}
