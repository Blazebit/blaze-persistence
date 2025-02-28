/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql;

/**
 * Metadata for {@link GraphQLDefaultFetch}.
 *
 * @author Christian Beikov
 * @since 1.6.15
 * @see GraphQLDefaultFetch
 */
public interface DefaultFetchMapping {
    /**
     * The name of the entity view attribute.
     *
     * @return entity view attribute name
     */
    public String getAttributeName();

    /**
     * Specifies the GraphQL field name that has to be present in the selection set to enable default fetching
     * of the annotated entity view attribute.
     *
     * @return The GraphQL field name based on which the fetch is activated
     */
    public String getIfFieldSelected();
}
