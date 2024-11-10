/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.entity;

import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.metamodel.ManagedViewType;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ReferenceEntityLoader extends AbstractEntityLoader {

    private final JpaProvider jpaProvider;
    private final String queryString;
    private final String queryStringMultiple;

    public ReferenceEntityLoader(EntityViewManagerImpl evm, ManagedViewType<?> subviewType, ViewToEntityMapper viewIdMapper) {
        this(evm, subviewType.getEntityClass(), jpaIdOf(evm, subviewType), viewIdMappingOf(evm, subviewType), viewIdMapper, evm.getEntityIdAccessor(), false);
    }

    public ReferenceEntityLoader(EntityViewManagerImpl evm, Class<?> entityClass, SingularAttribute<?, ?> idAttribute, SingularAttribute<?, ?> viewIdMappingAttribute, ViewToEntityMapper viewIdMapper, AttributeAccessor entityIdAccessor, boolean forceQuery) {
        super(evm, entityClass, idAttribute, viewIdMappingAttribute, viewIdMapper, entityIdAccessor);
        if (!forceQuery && primaryKeyId) {
            this.jpaProvider = null;
            this.queryString = null;
            this.queryStringMultiple = null;
        } else {
            this.jpaProvider = evm.getJpaProvider();
            this.queryString = "SELECT e FROM " + evm.getMetamodel().getEntityMetamodel().entity(entityClass).getName() + " e WHERE e." + idAttributeName + " = :id";
            this.queryStringMultiple = "SELECT e FROM " + evm.getMetamodel().getEntityMetamodel().entity(entityClass).getName() + " e WHERE e." + idAttributeName + " IN :entityIds";
        }
    }

    @Override
    public Object toEntity(UpdateContext context, Object view, Object id) {
        if (id == null) {
            return createEntity();
        }

        return getReferenceOrLoad(context, view, id);
    }

    @Override
    public void toEntities(UpdateContext context, List<Object> views, List<Object> ids) {
        getReferencesLoadOrCreate(context, views, ids);
    }

    @Override
    protected Object queryEntity(EntityManager em, Object id) {
        if (queryString == null) {
            return em.getReference(entityClass, id);
        }
        List<Object> list = em.createQuery(queryString)
                .setParameter("id", id)
                .getResultList();
        if (list.isEmpty()) {
            throw new EntityNotFoundException("Required entity '" + entityClass.getName() + "' with id '" + id + "' couldn't be found!");
        }

        Object entity = list.get(0);
        if (jpaProvider == null) {
            return entity;
        }
        // If we get here, it's most probably due to a Hibernate bug
        // To workaround it, we must actually unproxy the entity
        return jpaProvider.unproxy(entity);
    }

    @Override
    protected List<Object> queryEntities(EntityManager em, List<Object> ids) {
        if (queryStringMultiple == null) {
            List<Object> entities = new ArrayList<>(ids.size());
            for (Object id : ids) {
                entities.add(em.getReference(entityClass, id));
            }
            return entities;
        }
        List<Object> list = em.createQuery(queryStringMultiple)
            .setParameter("entityIds", ids)
            .getResultList();
        if (list.size() != ids.size()) {
            throw new EntityNotFoundException("Required entities '" + entityClass.getName() + "' with ids '" + ids + "' couldn't all be found!");
        }

        if (jpaProvider == null) {
            return list;
        }
        // If we get here, it's most probably due to a Hibernate bug
        // To workaround it, we must actually unproxy the entity
        for (int i = 0; i < list.size(); i++) {
            list.set(i, jpaProvider.unproxy(list.get(i)));
        }

        return list;
    }

    @Override
    public Object getEntityId(UpdateContext context, Object entity) {
        if (entityIdAccessor == null) {
            return null;
        }

        return entityIdAccessor.getValue(entity);
    }
}
