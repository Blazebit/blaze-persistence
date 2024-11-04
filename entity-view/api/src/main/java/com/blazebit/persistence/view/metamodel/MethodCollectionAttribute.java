/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

import java.util.Collection;

/**
 * A collection attribute that is also a method attribute.
 *
 * @param <X> The type of the declaring entity view
 * @param <E> The element type of the represented Collection
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface MethodCollectionAttribute<X, E> extends MethodPluralAttribute<X, Collection<E>, E>, CollectionAttribute<X, E> {
}
