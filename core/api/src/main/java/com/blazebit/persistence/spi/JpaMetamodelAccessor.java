/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;

import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.3.0
 */
public interface JpaMetamodelAccessor {

    /**
     * Construct an {@code AttributePath} for a particular attribute in type.
     * @param metamodel JPA metamodel
     * @param type Owning type
     * @param attributePath The attribute path
     * @return The created attribute path
     */
    AttributePath getAttributePath(Metamodel metamodel, ManagedType<?> type, String attributePath);

    /**
     * Construct an {@code AttributePath} for a particular basic attribute in type.
     * @param metamodel JPA metamodel
     * @param type Owning type
     * @param attributePath The attribute path
     * @return The created attribute path
     */
    AttributePath getBasicAttributePath(Metamodel metamodel, ManagedType<?> type, String attributePath);

    /**
     * Construct an {@code AttributePath} for a particular collection attribute in type.
     * @param metamodel JPA metamodel
     * @param type Owning type
     * @param attributePath The attribute path
     * @param collectionName The name of the collection
     * @return The created attribute path
     */
    AttributePath getJoinTableCollectionAttributePath(Metamodel metamodel, EntityType<?> type, String attributePath, String collectionName);

    /**
     * Returns true if the attribute is joinable (i.e. association).
     * @param attr The attribute
     * @return Whether the attribute is joinable
     */
    boolean isJoinable(Attribute<?, ?> attr);

    /**
     * Returns true if the attribute is composite (i.e. embeddable).
     * @param attr The attribute
     * @return Whether the attribute is composite
     */
    boolean isCompositeNode(Attribute<?, ?> attr);

    /**
     * Returns whether the given attribute is an element collection.
     *
     * @param attribute The attribute to check
     * @return true if the attribute is an element collection, false otherwise
     */
    boolean isElementCollection(Attribute<?, ?> attribute);
}
