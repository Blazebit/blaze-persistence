/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

import java.util.Set;

/**
 * A set attribute that is also a method attribute.
 *
 * @param <X> The type of the declaring entity view
 * @param <E> The element type of the represented Set
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface MethodSetAttribute<X, E> extends MethodPluralAttribute<X, Set<E>, E>, SetAttribute<X, E> {
}
