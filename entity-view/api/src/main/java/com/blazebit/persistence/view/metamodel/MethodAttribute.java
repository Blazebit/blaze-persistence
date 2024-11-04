/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

import com.blazebit.persistence.view.InverseRemoveStrategy;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Represents an attribute of a view type specified by a getter.
 *
 * @param <X> The type of the declaring entity view
 * @param <Y> The type of attribute
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface MethodAttribute<X, Y> extends Attribute<X, Y> {

    /**
     * Returns the name of this attribute.
     *
     * @return The name of this attribute
     */
    public String getName();

    /**
     * Returns the path by which the target type is <i>mapped by</i>.
     *
     * @return The mapped by path or null
     * @since 1.2.0
     */
    public String getMappedBy();

    /**
     * Returns the inverse remove strategy if this is an inverse mapped attribute.
     *
     * @return The inverse remove strategy
     * @since 1.2.0
     */
    public InverseRemoveStrategy getInverseRemoveStrategy();
    
    /**
     * Returns whether the attribute is updatable.
     * 
     * @return Whether the attribute is updatable
     * @since 1.1.0
     */
    public boolean isUpdatable();

    /**
     * Returns whether the attribute is mutable.
     * It is mutable if it is updatable or the target type is mutable.
     *
     * @return Whether the attribute is mutable
     * @since 1.2.0
     */
    public boolean isMutable();

    /**
     * Returns whether the attribute is protected by optimistic locking.
     *
     * @return Whether the attribute is optimistic lock protected
     * @since 1.2.0
     */
    public boolean isOptimisticLockProtected();

    /**
     * Returns whether the persisting of referenced objects is allowed.
     *
     * @return Whether persisting should be done
     * @since 1.2.0
     */
    public boolean isPersistCascaded();

    /**
     * Returns whether the updating of referenced objects is allowed.
     *
     * @return Whether updating should be done
     * @since 1.2.0
     */
    public boolean isUpdateCascaded();

    /**
     * Returns whether delete cascading for referenced objects should be done.
     *
     * @return Whether delete cascading should be done
     * @since 1.2.0
     */
    public boolean isDeleteCascaded();

    /**
     * Returns whether orphaned objects should be deleted during an update.
     *
     * @return Whether orphaned objects are deleted
     * @since 1.2.0
     */
    public boolean isOrphanRemoval();

    /**
     * Returns the read-only subtypes that are allowed to be assigned to this attribute.
     *
     * @return The allowed read-only subtypes for assigning
     * @since 1.3.0
     */
    public Set<Type<?>> getReadOnlyAllowedSubtypes();

    /**
     * Returns the subtypes that are allowed to be used when cascading {@link com.blazebit.persistence.view.CascadeType#PERSIST} events.
     *
     * @return The allowed subtypes for persist events
     * @since 1.2.0
     */
    public Set<Type<?>> getPersistCascadeAllowedSubtypes();

    /**
     * Returns the subtypes that are allowed to be used when cascading {@link com.blazebit.persistence.view.CascadeType#UPDATE} events.
     *
     * @return The allowed subtypes for update events
     * @since 1.2.0
     */
    public Set<Type<?>> getUpdateCascadeAllowedSubtypes();

    /**
     * Returns the subtypes that are allowed to be assigned to this attribute.
     *
     * @return The allowed subtypes for assigning
     * @since 1.5.0
     */
    public Set<Class<?>> getAllowedSubtypes();

    /**
     * Returns the updatable subtypes that need a parent to be assignable.
     *
     * @return The updatable subtypes that need a parent to be assignable
     * @since 1.5.0
     */
    public Set<Class<?>> getParentRequiringUpdateSubtypes();

    /**
     * Returns the creatable subtypes that need a parent to be assignable.
     *
     * @return The creatable subtypes that need a parent to be assignable
     * @since 1.5.0
     */
    public Set<Class<?>> getParentRequiringCreateSubtypes();

    /**
     * Returns the getter java method of this attribute.
     *
     * @return The getter java method of this attribute
     */
    public Method getJavaMethod();
    
    /**
     * Returns the attribute filter mapping of this attribute with the given name.
     * 
     * @param filterName The name of the attribute filter mapping which should be returned
     * @return The attribute filter mapping of this attribute with the given name
     */
    public AttributeFilterMapping<X, ?> getFilter(String filterName);
    
    /**
     * Returns the attribute filter mappings of this attribute.
     * 
     * @return The attribute filter mappings of this attribute
     */
    public Set<AttributeFilterMapping<X, ?>> getFilters();
}
