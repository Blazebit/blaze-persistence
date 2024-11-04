/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
