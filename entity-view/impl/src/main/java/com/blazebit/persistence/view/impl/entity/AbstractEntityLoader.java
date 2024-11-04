/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.entity;

import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.accessor.Accessors;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.SingularAttribute;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractEntityLoader implements EntityLoader {
    protected final Class<?> entityClass;
    protected final ViewToEntityMapper viewIdMapper;
    protected final Constructor<Object> entityConstructor;
    protected final String idAttributeName;
    protected final AttributeAccessor entityIdAccessor;
    protected final boolean primaryKeyId;

    public AbstractEntityLoader(EntityViewManagerImpl evm, Class<?> entityClass, SingularAttribute<?, ?> jpaIdAttribute, SingularAttribute<?, ?> viewIdMappingAttribute, ViewToEntityMapper viewIdMapper, AttributeAccessor entityIdAccessor) {
        this.entityClass = entityClass;
        this.viewIdMapper = viewIdMapper;
        try {
            Constructor<Object> constructor = (Constructor<Object>) entityClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            this.entityConstructor = constructor;
            if (jpaIdAttribute != null) {
                // The view maps an id attribute different from the entity id attribute
                if (viewIdMappingAttribute != null && !viewIdMappingAttribute.getName().equals(jpaIdAttribute.getName())) {
                    this.idAttributeName = viewIdMappingAttribute.getName();
                    this.entityIdAccessor = Accessors.forEntityMapping(evm, entityClass, viewIdMappingAttribute.getName());
                    this.primaryKeyId = false;
                } else {
                    this.idAttributeName = jpaIdAttribute.getName();
                    this.entityIdAccessor = entityIdAccessor;
                    this.primaryKeyId = true;
                }
            } else {
                this.idAttributeName = null;
                this.entityIdAccessor = null;
                this.primaryKeyId = true;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't find required no-arg constructor for entity class: " + entityClass.getName(), e);
        }
    }

    protected static jakarta.persistence.metamodel.SingularAttribute<?, ?> jpaIdOf(EntityViewManagerImpl evm, ManagedViewType<?> subviewType) {
        if (subviewType instanceof ViewType<?>) {
            return JpaMetamodelUtils.getSingleIdAttribute(evm.getMetamodel().getEntityMetamodel().entity(subviewType.getEntityClass()));
        }
        return null;
    }

    protected static jakarta.persistence.metamodel.SingularAttribute<?, ?> viewIdMappingOf(EntityViewManagerImpl evm, ManagedViewType<?> subviewType) {
        if (subviewType instanceof ViewType<?>) {
            ExtendedManagedType<?> managedType = evm.getMetamodel().getEntityMetamodel().getManagedType(ExtendedManagedType.class, subviewType.getEntityClass());
            return (SingularAttribute<?, ?>) managedType.getAttributes().get(((MappingAttribute) ((ViewType<?>) subviewType).getIdAttribute()).getMapping()).getAttribute();
        }
        return null;
    }

    protected static jakarta.persistence.metamodel.SingularAttribute<?, ?> associationIdMappingOf(EntityViewManagerImpl evm, ManagedViewType<?> subviewType, String attributeIdAttributeName) {
        if (subviewType instanceof ViewType<?>) {
            ExtendedManagedType<?> managedType = evm.getMetamodel().getEntityMetamodel().getManagedType(ExtendedManagedType.class, subviewType.getEntityClass());
            return (SingularAttribute<?, ?>) managedType.getAttributes().get(attributeIdAttributeName).getAttribute();
        }
        return null;
    }

    @Override
    public Class<?> getEntityClass() {
        return entityClass;
    }

    @Override
    public void toEntities(UpdateContext context, List<Object> views, List<Object> ids) {
        for (int i = 0; i < views.size(); i++) {
            views.set(i, toEntity(context, views.get(i), ids.get(i)));
        }
    }

    @Override
    public Object getEntityId(UpdateContext context, Object entity) {
        if (entityIdAccessor == null) {
            return null;
        }

        return entityIdAccessor.getValue(entity);
    }

    protected final Object createEntity() {
        try {
            return entityConstructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Couldn't map entity view to entity!", e);
        }
    }

    protected final Object getReferenceOrLoad(UpdateContext context, Object view, Object id) {
        id = getEntityId(context, view, id);
        if (primaryKeyId && context.containsEntity(entityClass, id)) {
            return context.getEntityManager().getReference(entityClass, id);
        } else {
            return queryEntity(context.getEntityManager(), id);
        }
    }

    protected final void getReferencesLoadOrCreate(UpdateContext context, List<Object> views, List<Object> ids) {
        List<Object> idsToQuery = null;
        if (primaryKeyId) {
            // If we have a primary key identifier, we might be able to refer to objects from the persistence context
            for (int i = 0; i < views.size(); i++) {
                Object view = views.get(i);
                Object id = ids.get(i);
                if (id == null) {
                    views.set(i, createEntity());
                } else {
                    id = getEntityId(context, view, id);
                    if (context.containsEntity(entityClass, id)) {
                        views.set(i, context.getEntityManager().getReference(entityClass, id));
                        ids.set(i, null);
                    } else {
                        if (idsToQuery == null) {
                            idsToQuery = new ArrayList<>(ids.size());
                        }
                        idsToQuery.add(id);
                    }
                }
            }
        } else {
            idsToQuery = new ArrayList<>(ids.size());
            for (int i = 0; i < views.size(); i++) {
                Object view = views.get(i);
                Object id = ids.get(i);
                if (id == null) {
                    views.set(i, createEntity());
                } else {
                    idsToQuery.add(getEntityId(context, view, id));
                }
            }
        }
        if (idsToQuery != null && !idsToQuery.isEmpty()) {
            List<Object> entities = queryEntities(context.getEntityManager(), idsToQuery);
            Map<Object, Object> entityIndex = new HashMap<>(entities.size());
            for (Object e : entities) {
                entityIndex.put(getEntityId(context, e), e);
            }
            for (int i = 0; i < views.size(); i++) {
                Object id = ids.get(i);
                if (id != null) {
                    Object entity = entityIndex.get(getEntityId(context, views.get(i), id));
                    views.set(i, entity);
                }
            }
        }
    }

    protected final Object getEntityId(UpdateContext context, Object view, Object id) {
        if (viewIdMapper != null) {
            id = viewIdMapper.applyToEntity(context, null, id);
        }
        return id;
    }

    protected abstract Object queryEntity(EntityManager em, Object id);

    protected abstract List<Object> queryEntities(EntityManager em, List<Object> ids);
}
