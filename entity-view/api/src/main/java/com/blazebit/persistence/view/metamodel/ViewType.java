/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

import com.blazebit.persistence.view.LockMode;

import java.util.Set;

/**
 * Represents the metamodel of an entity view.
 *
 * @param <X> The type of the entity view
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface ViewType<X> extends ManagedViewType<X> {

    /**
     * Returns the id attribute of the entity view.
     *
     * @return The id attribute of the entity view
     */
    public MethodAttribute<? super X, ?> getIdAttribute();

    /**
     * Returns the version attribute of the entity view if there is any, or null.
     *
     * @return The version attribute of the entity view, or null
     * @since 1.2.0
     */
    public MethodAttribute<? super X, ?> getVersionAttribute();

    /**
     * Returns path to the lock owner relative from the view types entity class if there is any, or null.
     *
     * @return The path to the lock owner, or null
     * @since 1.2.0
     */
    public String getLockOwner();

    /**
     * Returns the lock mode that is used for this entity view, or null.
     *
     * @return The lock mode, or null
     * @since 1.2.0
     */
    public LockMode getLockMode();
    
    /**
     * Returns the view filter mapping of the entity view with the given name.
     * 
     * @param filterName The name of the view filter mapping which should be returned
     * @return The view filter mapping of the entity view with the given name
     */
    public ViewFilterMapping getViewFilter(String filterName);
    
    /**
     * Returns the view filter mappings of the entity view.
     * 
     * @return The view filter mappings of the entity view
     */
    public Set<ViewFilterMapping> getViewFilters();
}
