/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A simple implementation of {@link PagedList} based on {@link ArrayList}.
 *
 * @param <T> the type of elements in this list
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.2.0
 */
public class PagedArrayList<T> extends ArrayList<T> implements PagedList<T> {

    private static final long serialVersionUID = 1L;

    private final KeysetPage keyset;
    private final long totalSize;
    private final int page;
    private final int totalPages;
    private final int firstResult;
    private final int maxResults;

    /**
     * Constructs a new empty paged array list.
     *
     * @param keyset      The keyset page for this page
     * @param totalSize   The total size of the result
     * @param firstResult The first result index within the overall result
     * @param maxResults  The maximum result count for a page
     */
    public PagedArrayList(KeysetPage keyset, long totalSize, int firstResult, int maxResults) {
        this.keyset = keyset;
        this.totalSize = totalSize;
        this.page = (int) Math.floor((firstResult == -1 ? 0 : firstResult) * 1d / maxResults) + 1;
        this.totalPages = totalSize < 1 ? 0 : (int) Math.ceil(totalSize * 1d / maxResults);
        this.firstResult = firstResult;
        this.maxResults = maxResults;
    }

    /**
     * Constructs a new paged array list from the given collection.
     *
     * @param collection  The collection of elements for this page
     * @param keyset      The keyset page for this page
     * @param totalSize   The total size of the result
     * @param firstResult The first result index within the overall result
     * @param maxResults  The maximum result count for a page
     */
    public PagedArrayList(Collection<? extends T> collection, KeysetPage keyset, long totalSize, int firstResult, int maxResults) {
        super(collection);
        this.keyset = keyset;
        this.totalSize = totalSize;
        this.page = (int) Math.floor((firstResult == -1 ? 0 : firstResult) * 1d / maxResults) + 1;
        this.totalPages = totalSize < 1 ? 0 : (int) Math.ceil(totalSize * 1d / maxResults);
        this.firstResult = firstResult;
        this.maxResults = maxResults;
    }

    @Override
    public int getSize() {
        return size();
    }

    @Override
    public long getTotalSize() {
        return totalSize;
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public int getTotalPages() {
        return totalPages;
    }

    @Override
    public int getFirstResult() {
        return firstResult;
    }

    @Override
    public int getMaxResults() {
        return maxResults;
    }

    @Override
    public KeysetPage getKeysetPage() {
        return keyset;
    }

}
