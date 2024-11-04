/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.DeleteCriteriaBuilder;
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.update.UpdateContext;

import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class UnmappedCollectionAttributeCascadeDeleter extends AbstractUnmappedAttributeCascadeDeleter {

    private final Class<?> ownerEntityClass;
    private final String ownerIdAttributeName;
    private final String mappedByAttributeName;
    private final boolean jpaProviderDeletesCollection;
    private final UnmappedBasicAttributeCascadeDeleter elementDeleter;

    public UnmappedCollectionAttributeCascadeDeleter(EntityViewManagerImpl evm, String attributeName, ExtendedAttribute<?, ?> attribute, Class<?> ownerEntityClass, String ownerIdAttributeName, boolean disallowCycle) {
        super(evm, attributeName, attribute);
        this.ownerEntityClass = ownerEntityClass;
        this.ownerIdAttributeName = ownerIdAttributeName;
        this.mappedByAttributeName = attribute.getMappedBy();
        JpaProvider jpaProvider = evm.getJpaProvider();
        if (elementIdAttributeName != null) {
            this.jpaProviderDeletesCollection = jpaProvider.supportsJoinTableCleanupOnDelete();
            if (cascadeDeleteElement) {
                this.elementDeleter = new UnmappedBasicAttributeCascadeDeleter(
                        evm,
                        "",
                        attribute,
                        ownerIdAttributeName,
                        disallowCycle
                );
            } else {
                this.elementDeleter = null;
            }
        } else {
            this.jpaProviderDeletesCollection = jpaProvider.supportsCollectionTableCleanupOnDelete();
            this.elementDeleter = null;
        }
    }

    private UnmappedCollectionAttributeCascadeDeleter(UnmappedCollectionAttributeCascadeDeleter original, boolean jpaProviderDeletesCollection) {
        super(original);
        this.ownerEntityClass = original.ownerEntityClass;
        this.ownerIdAttributeName = original.ownerIdAttributeName;
        this.mappedByAttributeName = original.mappedByAttributeName;
        this.jpaProviderDeletesCollection = jpaProviderDeletesCollection;
        this.elementDeleter = original.elementDeleter;
    }

    @Override
    public boolean requiresDeleteCascadeAfterRemove() {
        return false;
    }

    @Override
    public void removeById(UpdateContext context, Object id) {
        throw new UnsupportedOperationException("Can't delete collection attribute by id!");
    }

    @Override
    public void removeByOwnerId(UpdateContext context, Object ownerId) {
        EntityViewManagerImpl evm = context.getEntityViewManager();
        if (cascadeDeleteElement) {
            List<Object> elementIds;
            if (mappedByAttributeName == null) {
                // If there is no mapped by attribute, the collection has a join table
                if (evm.getDbmsDialect().supportsReturningColumns()) {
                    List<Tuple> tuples = evm.getCriteriaBuilderFactory().deleteCollection(context.getEntityManager(), ownerEntityClass, "e", attributeName)
                            .where(ownerIdAttributeName).eq(ownerId)
                            .executeWithReturning(attributeName + "." + elementIdAttributeName)
                            .getResultList();

                    elementIds = new ArrayList<>(tuples.size());
                    for (Tuple tuple : tuples) {
                        elementIds.add(tuple.get(0));
                    }
                } else {
                    elementIds = (List<Object>) evm.getCriteriaBuilderFactory().create(context.getEntityManager(), ownerEntityClass, "e")
                            .where(ownerIdAttributeName).eq(ownerId)
                            .select("e." + attributeName + "." + elementIdAttributeName)
                            .getResultList();
                    if (!elementIds.isEmpty()) {
                        // We must always delete this, otherwise we might get a constraint violation because of the cascading delete
                        DeleteCriteriaBuilder<?> cb = evm.getCriteriaBuilderFactory().deleteCollection(context.getEntityManager(), ownerEntityClass, "e", attributeName);
                        cb.where(ownerIdAttributeName).eq(ownerId);
                        cb.executeUpdate();
                    }
                }
                for (Object elementId : elementIds) {
                    elementDeleter.removeById(context, elementId);
                }
            } else {
                // Since there is a mapped by attribute, there is no join table to clear. Just delete the element by the owner id
                elementDeleter.removeByOwnerId(context, ownerId);
            }
        } else if (!jpaProviderDeletesCollection) {
            DeleteCriteriaBuilder<?> cb = evm.getCriteriaBuilderFactory().deleteCollection(context.getEntityManager(), ownerEntityClass, "e", attributeName);
            cb.where(ownerIdAttributeName).eq(ownerId);
            cb.executeUpdate();
        }
    }

    @Override
    public UnmappedAttributeCascadeDeleter createFlusherWiseDeleter() {
        return jpaProviderDeletesCollection ? new UnmappedCollectionAttributeCascadeDeleter(this, false) : this;
    }
}
