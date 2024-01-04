/*
 * Copyright 2014 - 2024 Blazebit.
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

import java.util.Collection;
import java.util.Map;

/**
 * A multi-map attribute that is also a method attribute.
 *
 * @param <X> The type of the declaring entity view
 * @param <K> The type of the key of the represented Map
 * @param <V> The type of the value of the represented Map
 * @param <C> The element collection type of the represented Map
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface MethodMultiMapAttribute<X, K, V, C extends Collection<V>> extends MethodPluralAttribute<X, Map<K, C>, C>, MapAttribute<X, K, C> {
}
