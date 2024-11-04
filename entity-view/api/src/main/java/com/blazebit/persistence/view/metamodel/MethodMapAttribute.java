/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

import java.util.Map;

/**
 * A map attribute that is also a method attribute.
 *
 * @param <X> The type of the declaring entity view
 * @param <K> The type of the key of the represented Map
 * @param <V> The type of the value of the represented Map
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface MethodMapAttribute<X, K, V> extends MethodPluralAttribute<X, Map<K, V>, V>, MapAttribute<X, K, V> {
}
