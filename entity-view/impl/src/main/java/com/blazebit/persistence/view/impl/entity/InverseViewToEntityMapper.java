/*
 * Copyright 2014 - 2017 Blazebit.
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

import com.blazebit.persistence.impl.util.JpaMetamodelUtils;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.accessor.Accessors;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.mapper.Mapper;
import com.blazebit.persistence.view.impl.proxy.EntityViewProxy;
import com.blazebit.persistence.view.impl.update.EntityViewUpdaterImpl;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.flush.DirtyAttributeFlusher;
import com.blazebit.persistence.view.metamodel.ViewType;

import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class InverseViewToEntityMapper<E> implements InverseElementToEntityMapper<E> {

    private static final String ID_PARAM_NAME = "_id";
    private final AttributeAccessor viewIdAccessor;
    private final EntityLoader entityLoader;
    private final boolean persistAllowed;

    private final Mapper<Object, Object> parentEntityOnChildViewMapper;
    private final Mapper<Object, Object> parentEntityOnChildEntityMapper;
    private final ViewToEntityMapper elementViewToEntityMapper;
    private final String updatePrefixString;
    private final String updatePostfixString;
    private final String fullUpdateQueryString;
    private final DirtyAttributeFlusher<?, Object, Object> parentReferenceAttributeFlusher;
    private final DirtyAttributeFlusher<?, Object, Object> idAttributeFlusher;

    public InverseViewToEntityMapper(EntityViewManagerImpl evm, ViewType<?> childViewType, Mapper<Object, Object> parentEntityOnChildViewMapper, Mapper<Object, Object> parentEntityOnChildEntityMapper,
                                     ViewToEntityMapper elementViewToEntityMapper, DirtyAttributeFlusher<?, Object, Object> parentReferenceAttributeFlusher, DirtyAttributeFlusher<?, Object, Object> idAttributeFlusher) {
        this.elementViewToEntityMapper = elementViewToEntityMapper;
        this.viewIdAccessor = Accessors.forViewId(evm, childViewType, true);
        // TODO: this should be the same loader that the viewToEntityMapper uses
        this.entityLoader = new ReferenceEntityLoader(evm, childViewType, EntityViewUpdaterImpl.createViewIdMapper(evm, childViewType));
        this.persistAllowed = false;
        EntityType<?> entityType = evm.getMetamodel().getEntityMetamodel().entity(childViewType.getEntityClass());
        this.parentEntityOnChildViewMapper = parentEntityOnChildViewMapper;
        this.parentEntityOnChildEntityMapper = parentEntityOnChildEntityMapper;
        this.updatePrefixString = "UPDATE " + entityType.getName() + " e SET ";
        this.updatePostfixString = " WHERE e." + JpaMetamodelUtils.getIdAttribute(entityType).getName() + " = :" + ID_PARAM_NAME;
        this.parentReferenceAttributeFlusher = parentReferenceAttributeFlusher;
        this.idAttributeFlusher = idAttributeFlusher;
        this.fullUpdateQueryString = createQueryString(null, parentReferenceAttributeFlusher);
    }

    @Override
    public void flushEntity(UpdateContext context, Object newParent, Object child, DirtyAttributeFlusher<?, E, Object> nestedGraphNode) {
        if (child == null) {
            return;
        }

        Object elementEntity = null;
        // Set the "newParent" on the view object "child"
        if (parentEntityOnChildViewMapper != null) {
            parentEntityOnChildViewMapper.map(context, newParent, child);
            if (shouldPersist(child)) {
                elementEntity = entityLoader.toEntity(context, null);
            }
            if (nestedGraphNode != null) {
                nestedGraphNode.flushEntity(context, (E) elementEntity, null, child);
            }
            elementViewToEntityMapper.applyToEntity(context, elementEntity, child);
        } else {
            Object id = viewIdAccessor.getValue(context, child);
            // If the view doesn't map the parent, we need to set it on the entity
            if (shouldPersist(child)) {
                elementEntity = entityLoader.toEntity(context, null);

                parentEntityOnChildEntityMapper.map(context, newParent, elementEntity);
                if (nestedGraphNode != null) {
                    nestedGraphNode.flushEntity(context, (E) elementEntity, null, child);
                }
                elementViewToEntityMapper.applyToEntity(context, elementEntity, child);
            } else {
                elementEntity = entityLoader.toEntity(context, id);

                parentEntityOnChildEntityMapper.map(context, newParent, elementEntity);
                if (nestedGraphNode != null) {
                    nestedGraphNode.flushEntity(context, (E) elementEntity, null, child);
                }
                elementViewToEntityMapper.applyToEntity(context, elementEntity, child);
            }
        }
    }

    protected boolean shouldPersist(Object view) {
        return view instanceof EntityViewProxy && ((EntityViewProxy) view).$$_isNew();
    }

    private String createQueryString(DirtyAttributeFlusher<?, ?, ?> nestedGraphNode, DirtyAttributeFlusher<?, ?, ?> inverseAttributeFlusher) {
        StringBuilder sb = new StringBuilder(updatePrefixString.length() + updatePostfixString.length() + 250);
        sb.append(updatePrefixString);
        inverseAttributeFlusher.appendUpdateQueryFragment(null, sb, null, null);

        if (nestedGraphNode != null) {
            sb.append(", ");
            int initialLength = sb.length();
            nestedGraphNode.appendUpdateQueryFragment(null, sb, null, null);

            if (sb.length() == initialLength) {
                sb.setLength(sb.length() - 2);
                sb.append(updatePostfixString);
                return sb.toString();
            } else {
                sb.append(updatePostfixString);
                return sb.toString();
            }
        }

        sb.append(updatePostfixString);
        return sb.toString();
    }

    @Override
    public Query createInverseUpdateQuery(UpdateContext context, Object view, DirtyAttributeFlusher<?, E, Object> nestedGraphNode, DirtyAttributeFlusher<?, ?, ?> inverseAttributeFlusher) {
        String queryString;
        if (inverseAttributeFlusher == this.parentReferenceAttributeFlusher && nestedGraphNode == null) {
            queryString = fullUpdateQueryString;
        } else {
            queryString = createQueryString(nestedGraphNode, inverseAttributeFlusher);
        }

        Query query = null;
        if (queryString != null) {
            query = context.getEntityManager().createQuery(queryString);
            query.setParameter(ID_PARAM_NAME, viewIdAccessor.getValue(context, view));
        }

        return query;
    }
}
