/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.UpdateCriteriaBuilder;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.accessor.Accessors;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.accessor.PassthroughAttributeAccessor;
import com.blazebit.persistence.view.impl.update.UpdateContext;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class UnmappedWritableBasicAttributeSetNullCascadeDeleter implements UnmappedAttributeCascadeDeleter {

    private static final Logger LOG = Logger.getLogger(UnmappedWritableBasicAttributeSetNullCascadeDeleter.class.getName());

    private final Class<?> ownerEntityClass;
    private final Map<String, String> removeByIdMappings;
    private final Map<String, ByOwnerIdEntry> removeByOwnerIdMappings;

    public UnmappedWritableBasicAttributeSetNullCascadeDeleter(EntityViewManagerImpl evm, ManagedType<?> ownerType, ExtendedManagedType<?> extendedManagedType, Map<String, String> writableMappings) {
        this.ownerEntityClass = extendedManagedType.getType().getJavaType();
        Map<String, String> removeByIdMappings = new HashMap<>(writableMappings.size());
        Map<String, ByOwnerIdEntry> removeByOwnerIdMappings = new HashMap<>(writableMappings.size());
        SingularAttribute<?, ?> idAttribute = null;
        String ownerIdAttributePrefix = null;
        if (ownerType instanceof EntityType<?>) {
            idAttribute = JpaMetamodelUtils.getSingleIdAttribute((EntityType<?>) ownerType);
            ownerIdAttributePrefix = JpaMetamodelUtils.getSingleIdAttribute((EntityType<?>) ownerType).getName() + ".";
        }
        for (Map.Entry<String, String> entry : writableMappings.entrySet()) {
            String elementIdAttribute = extendedManagedType.getIdAttribute().getName();
            removeByIdMappings.put(entry.getValue(), elementIdAttribute);

            Collection<String> idAttributes = evm.getJpaProvider().getJoinMappingPropertyNames((EntityType<?>) extendedManagedType.getType(), null, entry.getValue()).keySet();
            // NOTE: We ignore the fact that there might be multiple id attributes here because we currently support id class attributes yet anyway
            if (idAttributes.isEmpty()) {
                if (entry.getKey().startsWith(ownerIdAttributePrefix)) {
                    AttributeAccessor accessor = Accessors.forEntityMapping(evm, JpaMetamodelUtils.resolveFieldClass(ownerType.getJavaType(), idAttribute), entry.getKey().substring(ownerIdAttributePrefix.length()));
                    removeByOwnerIdMappings.put(entry.getValue(), new ByOwnerIdEntry(entry.getValue(), accessor));
                } else {
                    removeByOwnerIdMappings.put(entry.getValue(), new ByOwnerIdEntry("e." + idAttribute.getName(), ownerType.getJavaType(), "sub." + entry.getKey() + " = e." + entry.getValue(), PassthroughAttributeAccessor.INSTANCE));
                }
            } else {
                removeByOwnerIdMappings.put(entry.getValue(), new ByOwnerIdEntry(entry.getValue() + "." + idAttributes.iterator().next(), PassthroughAttributeAccessor.INSTANCE));
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
        for (Map.Entry<String, ByOwnerIdEntry> entry : removeByOwnerIdMappings.entrySet()) {
            updateCb.setExpression(entry.getKey(), "NULL");
            ByOwnerIdEntry value = entry.getValue();
            if (value.subqueryEntityClass == null) {
                updateCb.where(value.ownerIdMapping).eq(value.ownerIdAccessor.getValue(ownerId));
            } else {
                updateCb.whereExists()
                    .from(value.subqueryEntityClass, "sub")
                    .where(value.ownerIdMapping).eq(value.ownerIdAccessor.getValue(ownerId))
                    .end();
            }
        }
        updateCb.executeUpdate();
    }

    @Override
    public UnmappedAttributeCascadeDeleter createFlusherWiseDeleter() {
        return this;
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.4.0
     */
    private static class ByOwnerIdEntry {
        private final String ownerIdMapping;
        private final Class<?> subqueryEntityClass;
        private final String subqueryCorrelation;
        private final AttributeAccessor ownerIdAccessor;

        public ByOwnerIdEntry(String ownerIdMapping, AttributeAccessor ownerIdAccessor) {
            this.ownerIdMapping = ownerIdMapping;
            this.subqueryEntityClass = null;
            this.subqueryCorrelation = null;
            this.ownerIdAccessor = ownerIdAccessor;
        }

        public ByOwnerIdEntry(String ownerIdMapping, Class<?> subqueryEntityClass, String subqueryCorrelation, AttributeAccessor ownerIdAccessor) {
            this.ownerIdMapping = ownerIdMapping;
            this.subqueryEntityClass = subqueryEntityClass;
            this.subqueryCorrelation = subqueryCorrelation;
            this.ownerIdAccessor = ownerIdAccessor;
        }
    }
}
