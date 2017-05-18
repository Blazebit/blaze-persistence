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

package com.blazebit.persistence.spi;

import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Map;
import java.util.Set;

/**
 * This is a wrapper around the JPA {@link javax.persistence.metamodel.ManagedType} that allows additionally efficient access to properties of the metamodel.
 *
 * @param <X> The Java type represented by this managed type
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface ExtendedManagedType<X> {

    /**
     * Returns the underlying managed type.
     *
     * @return The managed type
     */
    public ManagedType<X> getType();

    /**
     * Returns whether the type has a cascading delete cycle.
     *
     * @return True if it has a cascading delete cycle, false otherwise
     */
    public boolean hasCascadingDeleteCycle();

    /**
     * Returns the id attribute if it has one, otherwise null.
     *
     * @return The id attribute or null
     */
    public SingularAttribute<X, ?> getIdAttribute();

    /**
     * Returns the id attributes or an empty set if it doesn't have an id.
     *
     * @return The id attributes
     * @since 1.3.0
     */
    public Set<SingularAttribute<X, ?>> getIdAttributes();

    /**
     * Returns the extended attributes of the managed type.
     *
     * @return The extended attributes
     */
    public Map<String, ExtendedAttribute<X, ?>> getAttributes();

    /**
     * Returns the extended attribute of the managed type for the given attribute name.
     *
     * @param attributeName The attribute name
     * @return The extended attributes
     * @throws IllegalArgumentException Is thrown when the attribute doesn't exist
     */
    public ExtendedAttribute<X, ?> getAttribute(String attributeName);
}

