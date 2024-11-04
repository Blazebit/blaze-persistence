/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
    private final CTEManager.CTEKey cteKey;
    private final Class<?> cteClass;
    private final X result;
    private final CTEBuilderListener listener;

    ReturningModificationCriteraBuilderFactoryImpl(MainQuery mainQuery, CTEManager.CTEKey cteKey, Class<?> cteClass, X result, final CTEBuilderListener listener) {
        this.mainQuery = mainQuery;
        this.cteKey = cteKey;
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
        ReturningDeleteCriteriaBuilderImpl<T, X> cb = new ReturningDeleteCriteriaBuilderImpl<T, X>(mainQuery, mainQuery.cteManager.getQueryContext(), deleteClass, alias, cteKey, cteClass, result, listener);
        listener.onBuilderStarted(cb);
        return cb;
    }

    @Override
    public <T> ReturningDeleteCriteriaBuilder<T, X> deleteCollection(Class<T> deleteOwnerClass, String collectionName) {
        return deleteCollection(deleteOwnerClass, null, collectionName);
    }

    @Override
    public <T> ReturningDeleteCriteriaBuilder<T, X> deleteCollection(Class<T> deleteOwnerClass, String alias, String collectionName) {
        ReturningDeleteCollectionCriteriaBuilderImpl<T, X> cb = new ReturningDeleteCollectionCriteriaBuilderImpl<T, X>(mainQuery, mainQuery.cteManager.getQueryContext(), deleteOwnerClass, alias, cteKey, cteClass, result, listener, collectionName);
        listener.onBuilderStarted(cb);
        return cb;
    }

    @Override
    public <T> ReturningUpdateCriteriaBuilder<T, X> update(Class<T> updateClass) {
        return update(updateClass, null);
    }

    @Override
    public <T> ReturningUpdateCriteriaBuilder<T, X> update(Class<T> updateClass, String alias) {
        ReturningUpdateCriteriaBuilderImpl<T, X> cb = new ReturningUpdateCriteriaBuilderImpl<T, X>(mainQuery, mainQuery.cteManager.getQueryContext(), updateClass, alias, cteKey, cteClass, result, listener);
        listener.onBuilderStarted(cb);
        return cb;
    }

    @Override
    public <T> ReturningUpdateCriteriaBuilder<T, X> updateCollection(Class<T> updateOwnerClass, String collectionName) {
        return updateCollection(updateOwnerClass, null, collectionName);
    }

    @Override
    public <T> ReturningUpdateCriteriaBuilder<T, X> updateCollection(Class<T> updateOwnerClass, String alias, String collectionName) {
        ReturningUpdateCollectionCriteriaBuilderImpl<T, X> cb = new ReturningUpdateCollectionCriteriaBuilderImpl<T, X>(mainQuery, mainQuery.cteManager.getQueryContext(), updateOwnerClass, alias, cteKey, cteClass, result, listener, collectionName);
        listener.onBuilderStarted(cb);
        return cb;
    }

    @Override
    public <T> ReturningInsertCriteriaBuilder<T, X> insert(Class<T> insertClass) {
        ReturningInsertCriteriaBuilderImpl<T, X> cb = new ReturningInsertCriteriaBuilderImpl<T, X>(mainQuery, mainQuery.cteManager.getQueryContext(), insertClass, cteKey, cteClass, result, listener);
        listener.onBuilderStarted(cb);
        return cb;
    }

    @Override
    public <T> ReturningInsertCriteriaBuilder<T, X> insertCollection(Class<T> insertOwnerClass, String collectionName) {
        ReturningInsertCollectionCriteriaBuilderImpl<T, X> cb = new ReturningInsertCollectionCriteriaBuilderImpl<T, X>(mainQuery, mainQuery.cteManager.getQueryContext(), insertOwnerClass, cteKey, cteClass, result, listener, collectionName);
        listener.onBuilderStarted(cb);
        return cb;
    }
}
