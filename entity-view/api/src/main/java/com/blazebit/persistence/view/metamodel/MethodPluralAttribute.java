/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

/**
 * A plural attribute that is also a method attribute.
 *
 * @param <X> The type of the declaring entity view
 * @param <C> The type of the represented collection
 * @param <E> The element type of the represented collection
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface MethodPluralAttribute<X, C, E> extends PluralAttribute<X, C, E>, MethodAttribute<X, C> {

}
