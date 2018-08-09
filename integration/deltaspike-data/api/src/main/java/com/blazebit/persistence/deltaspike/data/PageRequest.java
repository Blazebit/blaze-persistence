/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.deltaspike.data;

import java.io.Serializable;

/**
 * Immutable implementation of {@code Pageable}, heavily inspired by Spring Data's <code>PageRequest</code>.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PageRequest implements Pageable, Serializable {

    private final int offset;
    private final int size;
    private final Sort sort;

    /**
     * Creates a new {@link PageRequest} for requesting the given page with the given page size.
     *
     * @param page     The zero based page number
     * @param pageSize The number of elements per page
     */
    public PageRequest(int page, int pageSize) {
        this(page, pageSize, null);
    }

    /**
     * Creates a new {@link PageRequest} with a sorting based on the given paths and direction.
     *
     * @param page      The zero based page number
     * @param pageSize  The number of elements per page
     * @param direction The direction to use for the {@link Sort} to be specified, may be <code>null</code>
     * @param paths     The paths to sort by
     */
    public PageRequest(int page, int pageSize, Sort.Direction direction, String... paths) {
        this(page, pageSize, new Sort(direction, paths));
    }

    /**
     * Creates a new {@link PageRequest} with given sorting.
     *
     * @param page     The zero based page number
     * @param pageSize The number of elements per page
     * @param sort     The sorting to use for the page, may be <code>null</code>
     */
    public PageRequest(int page, int pageSize, Sort sort) {
        this(sort, page * pageSize, pageSize);
    }

    /**
     * Creates a new {@link PageRequest} with given sorting.
     *
     * @param sort     The sorting to use for the page, may be <code>null</code>
     * @param offset   The zero based offset number
     * @param pageSize The number of elements per page
     * @since 1.3.0
     */
    public PageRequest(Sort sort, int offset, int pageSize) {
        if (offset < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero!");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page pageSize must not be less than one!");
        }

        this.offset = offset;
        this.size = pageSize;
        this.sort = sort;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new PageRequest(getSort(), getOffset() + getPageSize(), getPageSize());
    }

    @Override
    public Pageable first() {
        return new PageRequest(getSort(), 0, getPageSize());
    }

    @Override
    public int getPageSize() {
        return size;
    }

    @Override
    public int getPageNumber() {
        return offset / size;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public boolean hasPrevious() {
        return offset > 0;
    }

    @Override
    public Pageable previous() {
        if (getOffset() == 0) {
            return null;
        }
        return new PageRequest(getSort(), getOffset() - getPageSize(), getPageSize());
    }

    @Override
    public Pageable previousOrFirst() {
        if (getOffset() == 0) {
            return this;
        }
        return new PageRequest(getSort(), getOffset() - getPageSize(), getPageSize());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Pageable)) {
            return false;
        }

        Pageable that = (Pageable) o;

        if (offset != that.getOffset()) {
            return false;
        }
        if (size != that.getPageSize()) {
            return false;
        }
        return sort != null ? sort.equals(that.getSort()) : that.getSort() == null;
    }

    @Override
    public int hashCode() {
        int result = offset;
        result = 31 * result + size;
        result = 31 * result + (sort != null ? sort.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Page request [number: " + getPageNumber() + ", size " + getPageSize() + ", sort: " + sort + "]";
    }
}
