/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class UnmappedAttributeCascadeDeleterUtil {

    private UnmappedAttributeCascadeDeleterUtil() {
    }

    public static List<UnmappedAttributeCascadeDeleter> createUnmappedCascadeDeleters(EntityViewManagerImpl evm, Class<?> entityClass, String ownerIdAttributeName) {
        EntityMetamodel entityMetamodel = evm.getMetamodel().getEntityMetamodel();
        ExtendedManagedType<?> extendedManagedType = entityMetamodel.getManagedType(ExtendedManagedType.class, entityClass);
        Map<String, ? extends ExtendedAttribute<?, ?>> attributes = extendedManagedType.getOwnedAttributes();
        List<UnmappedAttributeCascadeDeleter> deleters = new ArrayList<>(attributes.size());

        for (Map.Entry<String, ? extends ExtendedAttribute<?, ?>> entry : attributes.entrySet()) {
            ExtendedAttribute<?, ?> extendedAttribute = entry.getValue();
            if (extendedAttribute.getAttribute().isCollection()) {
                if (((jakarta.persistence.metamodel.PluralAttribute<?, ?, ?>) extendedAttribute.getAttribute()).getCollectionType() == jakarta.persistence.metamodel.PluralAttribute.CollectionType.MAP) {
                    deleters.add(new UnmappedMapAttributeCascadeDeleter(
                            evm,
                            entry.getKey(),
                            extendedAttribute,
                            entityClass,
                            ownerIdAttributeName,
                            true
                    ));
                } else {
                    deleters.add(new UnmappedCollectionAttributeCascadeDeleter(
                            evm,
                            entry.getKey(),
                            extendedAttribute,
                            entityClass,
                            ownerIdAttributeName,
                            true
                    ));
                }
            } else if (JpaMetamodelUtils.isAssociation(extendedAttribute.getAttribute()) && extendedAttribute.isDeleteCascaded()) {
                deleters.add(new UnmappedBasicAttributeCascadeDeleter(
                        evm,
                        entry.getKey(),
                        extendedAttribute,
                        ownerIdAttributeName,
                        true
                ));
            }
        }

        return deleters;
    }

}
