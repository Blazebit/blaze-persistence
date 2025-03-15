/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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

    public static final transient GraphQLRelayPageInfo EMPTY = new GraphQLRelayPageInfo();

    private final boolean hasNextPage;
    private final boolean hasPreviousPage;
    private final String startCursor;
    private final String endCursor;

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
        this.hasNextPage = data.size() >= data.getMaxResults() && (data.getTotalSize() == -1 || data.getFirstResult() + data.getMaxResults() < data.getTotalSize());
        KeysetPage keysetPage = data.getKeysetPage();
        if (keysetPage != null && keysetPage.getLowest() != null) {
            this.startCursor = Base64.getEncoder().encodeToString(serialize(data.getFirstResult(), data.getMaxResults(), keysetPage.getLowest().getTuple()));
        } else {
            this.startCursor = null;
        }
        if (keysetPage != null && keysetPage.getHighest() != null) {
            this.endCursor = Base64.getEncoder().encodeToString(serialize(data.getFirstResult(), data.getMaxResults(), keysetPage.getHighest().getTuple()));
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
        return startCursor;
    }

    /**
     * Returns the end cursor encoded as Base64 or <code>null</code>.
     *
     * @return the end cursor or <code>null</code>
     */
    public String getEndCursor() {
        return endCursor;
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
