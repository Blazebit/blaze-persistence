/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

/**
 * A singular attribute that is also a method attribute.
 *
 * @param <X> The type of the declaring entity view
 * @param <Y> The type of attribute
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface MethodSingularAttribute<X, Y> extends SingularAttribute<X, Y>, MethodAttribute<X, Y> {

}
