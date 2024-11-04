/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

import java.util.Map;

/**
 * Instances of the type {@linkplain SingularAttribute} represents single-valued properties or fields.
 *
 * @param <X> The type of the declaring entity view
 * @param <Y> The type of attribute
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface SingularAttribute<X, Y> extends Attribute<X, Y> {

    /**
     * Returns the type representing the type of the attribute.
     *
     * @return The type of the attribute
     * @since 1.2.0
     */
    public Type<Y> getType();

    /**
     * Returns the inheritance subtype mappings that should be considered for this attribute.
     * When the attribute type is not a subview, this returns an empty map.
     *
     * @return The inheritance subtype mappings or an empty map
     * @since 1.2.0
     */
    public Map<ManagedViewType<? extends Y>, String> getInheritanceSubtypeMappings();

    /**
     * Returns true if this attribute maps to a query parameter, otherwise false.
     *
     * @return True if this attribute maps to a query parameter, otherwise false
     */
    public boolean isQueryParameter();
    
    /**
     * Returns true if this attribute maps to the entity id, otherwise false.
     * 
     * @return True if this attribute maps to the entity id, otherwise false
     */
    public boolean isId();

    /**
     * Returns true if an empty flat view should be created for this attribute, otherwise false.
     *
     * @return True if an empty flat view should be created for this attribute, otherwise false
     * @since 1.5.0
     */
    public boolean isCreateEmptyFlatView();
}
