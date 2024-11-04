/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria;

import javax.persistence.criteria.Root;

/**
 * An extended version of {@link Root}.
 *
 * @param <X> the entity type referenced by the root
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BlazeRoot<X> extends Root<X>, BlazeFrom<X, X> {

    /**
     * Treats this from object as the given subtype. This will not cause a separate join but return a wrapper,
     * that can be used for further joins.
     *
     * @param type type to be downcast to
     * @param <T>  The target treat type
     * @return The treated from object
     */
    <T extends X> BlazeRoot<T> treatAs(Class<T> type);
}
