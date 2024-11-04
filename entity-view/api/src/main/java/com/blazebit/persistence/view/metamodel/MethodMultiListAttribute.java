/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

import java.util.Collection;
import java.util.List;

/**
 * A multi-list attribute that is also a method attribute.
 *
 * @param <X> The type of the declaring entity view
 * @param <E> The element type of the represented List
 * @param <C> The element collection type of the represented List
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface MethodMultiListAttribute<X, E, C extends Collection<E>> extends MethodPluralAttribute<X, List<C>, C>, ListAttribute<X, C> {
}
