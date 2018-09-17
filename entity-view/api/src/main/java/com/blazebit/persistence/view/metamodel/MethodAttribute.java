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
    public AttributeFilterMapping getFilter(String filterName);
    
    /**
     * Returns the attribute filter mappings of this attribute.
     * 
     * @return The attribute filter mappings of this attribute
     */
    public Set<AttributeFilterMapping> getFilters();
}
