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
 * Instances of the type {@linkplain MapAttribute} represent persistent {@linkplain java.util.Map}-valued attributes.
 *
 * @param <X> The type of the declaring entity view
 * @param <K> The type of the key of the represented Map
 * @param <V> The type of the value of the represented Map
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface MapAttribute<X, K, V> extends PluralAttribute<X, Map<K, V>, V> {

    /**
     * Returns the java type of the key.
     *
     * @return The java type of the key
     */
    public Type<K> getKeyType();

    /**
     * Returns the inheritance subtypes that should be considered for the keys of this map attribute.
     * When the key type of the map attribute is not a subview, this returns an empty set.
     *
     * @return The inheritance subtypes or an empty set
     * @since 1.2.0
     */
    public Map<ManagedViewType<? extends K>, String> getKeyInheritanceSubtypeMappings();

    /**
     * Returns true if the key of this map attribute is a subview, otherwise false.
     *
     * @return True if the key of this map attribute is a subview, otherwise false
     * @since 1.2.0
     */
    public boolean isKeySubview();
}
