/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria;

import javax.persistence.criteria.Order;

/**
 * An extended version of {@link Order}.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BlazeOrder extends Order {

    /**
     * Switch the null precedence.
     *
     * @return A new <code>BlazeOrder</code> instance with the reversed null precedence
     */
    BlazeOrder reverseNulls();

    /**
     * Whether nulls come first.
     *
     * @return True if nulls come first, false otherwise
     */
    boolean isNullsFirst();

    /* covariant overrides */

    /**
     * Switch the ordering.
     *
     * @return A new <code>BlazeOrder</code> instance with the reversed ordering
     */
    BlazeOrder reverse();

}
