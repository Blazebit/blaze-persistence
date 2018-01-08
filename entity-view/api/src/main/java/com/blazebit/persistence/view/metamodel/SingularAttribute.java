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

}
