/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.view.impl.entity;

import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.flush.DirtyAttributeFlusher;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class FlusherBasedEntityLoader extends AbstractEntityLoader {

    private final DirtyAttributeFlusher<?, Object, Object>[] flushers;
    private volatile String queryString;
    private volatile String queryStringMultiple;

    public FlusherBasedEntityLoader(EntityViewManagerImpl evm, Class<?> entityClass, SingularAttribute<?, ?> jpaIdAttribute, ViewToEntityMapper viewIdMapper, AttributeAccessor entityIdAccessor, DirtyAttributeFlusher<?, Object, Object>[] flushers) {
        super(evm, entityClass, jpaIdAttribute, null, viewIdMapper, entityIdAccessor);
        this.flushers = flushers;
        // TODO: optimize by copying more from existing loaders and avoid object allocations
        // TODO: consider constructing query eagerly,
    }

    private String getQueryString() {
        String query = queryString;
        if (query != null) {
            return query;
        }

        StringBuilder sb = new StringBuilder();

        sb.append("SELECT e FROM ").append(entityClass.getName()).append(" e");
        for (int i = 0; i < flushers.length; i++) {
            if (flushers[i] != null) {
                flushers[i].appendFetchJoinQueryFragment("e", sb);
            }
        }
        sb.append(" WHERE e.").append(idAttributeName).append(" = :id");

        query = sb.toString();
        queryString = query;
        return query;
    }

    private String getQueryStringMultiple() {
        String query = queryStringMultiple;
        if (query != null) {
            return query;
        }

        StringBuilder sb = new StringBuilder();

        sb.append("SELECT e FROM ").append(entityClass.getName()).append(" e");
        for (int i = 0; i < flushers.length; i++) {
            if (flushers[i] != null) {
                flushers[i].appendFetchJoinQueryFragment("e", sb);
            }
        }
        sb.append(" WHERE e.").append(idAttributeName).append(" IN :entityIds");

        query = sb.toString();
        queryStringMultiple = query;
        return query;
    }

    @Override
    public Object toEntity(UpdateContext context, Object view, Object id) {
        if (id == null || entityIdAccessor == null) {
            return createEntity();
        }

        return getReferenceOrLoad(context, view, id);
    }

    @Override
    public void toEntities(UpdateContext context, List<Object> views, List<Object> ids) {
        if (entityIdAccessor == null) {
            for (int i = 0; i < views.size(); i++) {
                views.set(i, createEntity());
            }
        } else {
            getReferencesLoadOrCreate(context, views, ids);
        }
    }

    @Override
    protected Object queryEntity(EntityManager em, Object id) {
        @SuppressWarnings("unchecked")
        List<Object> list = em.createQuery(getQueryString())
                .setParameter("id", id)
                .getResultList();
        if (list.isEmpty()) {
            throw new EntityNotFoundException("Required entity '" + entityClass.getName() + "' with id '" + id + "' couldn't be found!");
        }

        return list.get(0);
    }

    @Override
    protected List<Object> queryEntities(EntityManager em, List<Object> ids) {
        List<Object> list = em.createQuery(getQueryStringMultiple())
            .setParameter("entityIds", ids)
            .getResultList();
        if (list.size() != ids.size()) {
            throw new EntityNotFoundException("Required entities '" + entityClass.getName() + "' with ids '" + ids + "' couldn't all be found!");
        }

        return list;
    }
}
