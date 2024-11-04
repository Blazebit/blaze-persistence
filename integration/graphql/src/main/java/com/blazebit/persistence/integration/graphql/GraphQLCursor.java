/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql;

import java.io.Serializable;


/**
 * A GraphQL representation of cursor.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class GraphQLCursor implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int offset;
    private final int pageSize;
    private final Serializable[] tuple;

    /**
     * Creates a new cursor for the given offset, page size and tuple.
     *
     * @param offset The offset
     * @param pageSize The page size
     * @param tuple The tuple
     */
    public GraphQLCursor(int offset, int pageSize, Serializable[] tuple) {
        this.offset = offset;
        this.pageSize = pageSize;
        this.tuple = tuple;
    }

    /**
     * The offset for which the cursor was created.
     *
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * The page size for which the cursor was created.
     *
     * @return the page size
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * The tuple representing the content of the cursor.
     *
     * @return the tuple
     */
    public Serializable[] getTuple() {
        return tuple;
    }
}
