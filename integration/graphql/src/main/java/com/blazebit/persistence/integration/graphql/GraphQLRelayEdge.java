/*
 * Copyright 2014 - 2020 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
