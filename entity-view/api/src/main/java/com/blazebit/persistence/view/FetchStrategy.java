/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

/**
 * The fetch strategy for an entity view attribute.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public enum FetchStrategy {

    /**
     * A strategy that defines that the target elements are joined and fetched along in the source query.
     */
    JOIN,
    /**
     * A strategy that defines that the target elements are selected in separate queries. Depending on the defined {@link BatchFetch#size()}, the query select multiple elements at once.
     */
    SELECT,
    /**
     * A strategy that defines that the target elements are selected in a single query containing the source query as subquery.
     */
    SUBSELECT,
    /**
     * A strategy that defines that the target elements are joined and fetched along in the source query as multiset.
     *
     * @since 1.5.0
     */
    MULTISET;
}
