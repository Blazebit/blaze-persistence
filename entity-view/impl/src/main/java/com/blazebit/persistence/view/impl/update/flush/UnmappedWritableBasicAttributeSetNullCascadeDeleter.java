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

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.UpdateCriteriaBuilder;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.update.UpdateContext;

import javax.persistence.metamodel.EntityType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class UnmappedWritableBasicAttributeSetNullCascadeDeleter implements UnmappedAttributeCascadeDeleter {

    private final Class<?> ownerEntityClass;
    private final Map<String, String> removeByIdMappings;
    private final Map<String, String> removeByOwnerIdMappings;

    public UnmappedWritableBasicAttributeSetNullCascadeDeleter(EntityViewManagerImpl evm, ExtendedManagedType<?> extendedManagedType, Map<String, String> writableMappings) {
        this.ownerEntityClass = extendedManagedType.getType().getJavaType();
        Map<String, String> removeByIdMappings = new HashMap<>(writableMappings.size());
        Map<String, String> removeByOwnerIdMappings = new HashMap<>(writableMappings.size());
        for (Map.Entry<String, String> entry : writableMappings.entrySet()) {
            String ownerIdAttribute = extendedManagedType.getIdAttribute().getName();
            List<String> idAttributes = evm.getJpaProvider().getIdentifierOrUniqueKeyEmbeddedPropertyNames((EntityType<?>) extendedManagedType.getType(), entry.getValue());
            removeByIdMappings.put(entry.getValue(), ownerIdAttribute);
            // NOTE: We ignore the fact that there might be multiple id attributes here because we currently support id class attributes yet anyway
            if (!idAttributes.isEmpty()) {
                removeByOwnerIdMappings.put(entry.getValue(), entry.getValue() + "." + idAttributes.get(0));
            }
        }


        this.removeByIdMappings = removeByIdMappings;
        this.removeByOwnerIdMappings = removeByOwnerIdMappings;
    }

    @Override
    public String getAttributeValuePath() {
        return null;
    }

    @Override
    public boolean requiresDeleteCascadeAfterRemove() {
        return false;
    }

    @Override
    public void removeById(UpdateContext context, Object id) {
        UpdateCriteriaBuilder<?> updateCb = context.getEntityViewManager().getCriteriaBuilderFactory().update(context.getEntityManager(), ownerEntityClass, "e");
        for (Map.Entry<String, String> entry : removeByIdMappings.entrySet()) {
            updateCb.setExpression(entry.getKey(), "NULL");
            updateCb.where(entry.getValue()).eq(id);
        }
        updateCb.executeUpdate();
    }

    @Override
    public void removeByOwnerId(UpdateContext context, Object ownerId) {
        UpdateCriteriaBuilder<?> updateCb = context.getEntityViewManager().getCriteriaBuilderFactory().update(context.getEntityManager(), ownerEntityClass, "e");
        for (Map.Entry<String, String> entry : removeByOwnerIdMappings.entrySet()) {
            updateCb.setExpression(entry.getKey(), "NULL");
            updateCb.where(entry.getValue()).eq(ownerId);
        }
        updateCb.executeUpdate();
    }

    @Override
    public UnmappedAttributeCascadeDeleter createFlusherWiseDeleter() {
        return this;
    }
}
