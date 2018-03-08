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

import com.blazebit.persistence.parser.EntityMetamodel;
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
        Map<String, ? extends ExtendedAttribute<?, ?>> attributes = extendedManagedType.getAttributes();
        List<UnmappedAttributeCascadeDeleter> deleters = new ArrayList<>(attributes.size());

        for (Map.Entry<String, ? extends ExtendedAttribute<?, ?>> entry : attributes.entrySet()) {
            ExtendedAttribute<?, ?> extendedAttribute = entry.getValue();
            if (extendedAttribute.getAttribute().isCollection()) {
                if (((javax.persistence.metamodel.PluralAttribute<?, ?, ?>) extendedAttribute.getAttribute()).getCollectionType() == javax.persistence.metamodel.PluralAttribute.CollectionType.MAP) {
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
            } else if (extendedAttribute.getAttribute().isAssociation() && extendedAttribute.isDeleteCascaded()) {
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
