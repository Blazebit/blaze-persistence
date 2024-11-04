/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql;

import java.io.Serializable;

/**
 * A GraphQL Relay Edge.
 *
 * @param <T> The content type of the edge
 * @author Christian Beikov
 * @since 1.4.0
 */
public class GraphQLRelayEdge<T> implements Serializable {

    private final String cursor;
    private final T node;

    /**
     * Creates a new node.
     *
     * @param cursor The cursor
     * @param node The node
     */
    public GraphQLRelayEdge(String cursor, T node) {
        this.cursor = cursor;
        this.node = node;
    }

    /**
     * Returns the cursor for the node encoded in Base64.
     *
     * @return the cursor for the node
     */
    public String getCursor() {
        return cursor;
    }

    /**
     * Returns the node for this edge.
     *
     * @return the node
     */
    public T getNode() {
        return node;
    }
}
