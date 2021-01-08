/*
 * Copyright 2014 - 2021 Blazebit.
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
