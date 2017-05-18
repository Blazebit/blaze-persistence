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

package com.blazebit.persistence.view.impl.entity;

import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.mapper.Mapper;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.flush.DirtyAttributeFlusher;

import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class InverseEntityToEntityMapper<E> implements InverseElementToEntityMapper<E> {

    private static final String ID_PARAM_NAME = "_id";
    private final String updatePrefixString;
    private final String updatePostfixString;
    private final String fullUpdateQueryString;
    private final AttributeAccessor entityIdAccessor;
    private final DirtyAttributeFlusher<?, ?, ?> inverseAttributeFlusher;
    private final Mapper<Object, Object> parentEntityOnChildEntityMapper;

    public InverseEntityToEntityMapper(EntityViewManagerImpl evm, EntityType<?> entityType, Mapper<Object, Object> parentEntityOnChildEntityMapper, DirtyAttributeFlusher<?, ?, ?> inverseAttributeFlusher) {
        this.updatePrefixString = "UPDATE " + entityType.getName() + " e SET ";
        this.updatePostfixString = " WHERE e." + JpaMetamodelUtils.getSingleIdAttribute(entityType).getName() + " = :" + ID_PARAM_NAME;
        this.parentEntityOnChildEntityMapper = parentEntityOnChildEntityMapper;
        this.inverseAttributeFlusher = inverseAttributeFlusher;
        this.fullUpdateQueryString = createQueryString(null, inverseAttributeFlusher);
        this.entityIdAccessor = evm.getEntityIdAccessor();
    }

    private String createQueryString(DirtyAttributeFlusher<?, ?, ?> nestedGraphNode, DirtyAttributeFlusher<?, ?, ?> inverseAttributeFlusher) {
        StringBuilder sb = new StringBuilder(updatePrefixString.length() + updatePostfixString.length() + 250);
        sb.append(updatePrefixString);
        inverseAttributeFlusher.appendUpdateQueryFragment(null, sb, null, null, ", ");

        if (nestedGraphNode != null) {
            sb.append(", ");
            int initialLength = sb.length();
            nestedGraphNode.appendUpdateQueryFragment(null, sb, null, null, ", ");

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
    public void flushEntity(UpdateContext context, Object newParent, Object child, DirtyAttributeFlusher<?, E, Object> nestedGraphNode) {
        // Set the "newParent" on the entity object "child"
        parentEntityOnChildEntityMapper.map(newParent, child);

        if (nestedGraphNode != null) {
            nestedGraphNode.flushEntity(context, null, null, child, null);
        }
    }

    @Override
    public Query createInverseUpdateQuery(UpdateContext context, Object element, DirtyAttributeFlusher<?, E, Object> nestedGraphNode, DirtyAttributeFlusher<?, ?, ?> inverseAttributeFlusher) {
        String queryString;
        if (inverseAttributeFlusher == this.inverseAttributeFlusher && nestedGraphNode == null) {
            queryString = fullUpdateQueryString;
        } else {
            queryString = createQueryString(nestedGraphNode, inverseAttributeFlusher);
        }

        Query query = null;
        if (queryString != null) {
            query = context.getEntityManager().createQuery(queryString);
            query.setParameter(ID_PARAM_NAME, entityIdAccessor.getValue(element));
        }

        return query;
    }
}
