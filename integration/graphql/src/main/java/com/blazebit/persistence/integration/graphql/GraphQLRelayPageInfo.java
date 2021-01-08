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

import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.PagedList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;

/**
 * A GraphQL Relay page info.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class GraphQLRelayPageInfo implements Serializable {

    public static final GraphQLRelayPageInfo EMPTY = new GraphQLRelayPageInfo();

    private final boolean hasNextPage;
    private final boolean hasPreviousPage;
    private final GraphQLCursor startCursor;
    private final GraphQLCursor endCursor;

    private GraphQLRelayPageInfo() {
        this.hasNextPage = true;
        this.hasPreviousPage = true;
        this.startCursor = null;
        this.endCursor = null;
    }

    /**
     * Creates a new page info object from the given paged list.
     *
     * @param data The paged list
     */
    public GraphQLRelayPageInfo(PagedList<?> data) {
        this.hasPreviousPage = data.getFirstResult() != 0;
        this.hasNextPage = data.getTotalSize() == -1 || data.getFirstResult() + data.getMaxResults() < data.getTotalSize();
        KeysetPage keysetPage = data.getKeysetPage();
        if (keysetPage != null && keysetPage.getLowest() != null) {
            this.startCursor = new GraphQLCursor(data.getFirstResult(), data.getMaxResults(), keysetPage.getLowest().getTuple());
        } else {
            this.startCursor = null;
        }
        if (keysetPage != null && keysetPage.getHighest() != null) {
            this.endCursor = new GraphQLCursor(data.getFirstResult(), data.getMaxResults(), keysetPage.getHighest().getTuple());
        } else {
            this.endCursor = null;
        }
    }

    /**
     * Returns whether there is a next page.
     *
     * @return whether there is a next page
     */
    public boolean isHasNextPage() {
        return hasNextPage;
    }

    /**
     * Returns whether there is a previous page.
     *
     * @return whether there is a previous page
     */
    public boolean isHasPreviousPage() {
        return hasPreviousPage;
    }

    /**
     * Returns the start cursor encoded as Base64 or <code>null</code>.
     *
     * @return the start cursor or <code>null</code>
     */
    public String getStartCursor() {
        if (startCursor == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(serialize(startCursor));
    }

    /**
     * Returns the end cursor encoded as Base64 or <code>null</code>.
     *
     * @return the end cursor or <code>null</code>
     */
    public String getEndCursor() {
        if (endCursor == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(serialize(endCursor));
    }

    /**
     * Serializes the given cursor to a byte array.
     *
     * @param cursor The cursor
     * @return the serialized form of the cursor
     */
    protected byte[] serialize(GraphQLCursor cursor) {
        return serializeCursor(cursor.getOffset(), cursor.getPageSize(), cursor.getTuple());
    }

    /**
     * Serializes the given cursor components to a byte array.
     *
     * @param offset The offset
     * @param pageSize The page size
     * @param tuple The tuple
     * @return the serialized form of the cursor
     */
    protected byte[] serialize(int offset, int pageSize, Serializable[] tuple) {
        return serializeCursor(offset, pageSize, tuple);
    }

    /**
     * Serializes the given cursor components to a byte array.
     *
     * @param offset The offset
     * @param pageSize The page size
     * @param tuple The tuple
     * @return the serialized form of the cursor
     */
    protected static byte[] serializeCursor(int offset, int pageSize, Serializable[] tuple) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.write(offset);
            oos.write(pageSize);
            oos.writeObject(tuple);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }
}
