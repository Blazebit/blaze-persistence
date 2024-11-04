/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * A builder for the like predicate.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public interface LikeBuilder<T> extends BinaryPredicateBuilder<EscapeBuilder<T>> {

}
