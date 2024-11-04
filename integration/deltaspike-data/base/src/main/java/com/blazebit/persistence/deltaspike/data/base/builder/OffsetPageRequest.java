/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.base.builder;

import com.blazebit.persistence.deltaspike.data.Pageable;
import com.blazebit.persistence.deltaspike.data.Sort;

import java.io.Serializable;

/**
 * Immutable implementation of {@code Pageable}, working with offset rather than page semantics.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class OffsetPageRequest implements Pageable, Serializable {

    private final int offset;
    private final int size;

    public OffsetPageRequest(int offset, int pageSize) {
        this.offset = offset;
        this.size = pageSize;
    }

    @Override
    public Sort getSort() {
        return null;
    }

    @Override
    public Pageable next() {
        return new OffsetPageRequest(offset + size, size);
    }

    @Override
    public Pageable first() {
        return new OffsetPageRequest(0, size);
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
        return new OffsetPageRequest(offset - size, size);
    }

    @Override
    public Pageable previousOrFirst() {
        if (getOffset() == 0) {
            return this;
        }
        return new OffsetPageRequest(offset - size, size);
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
        return that.getSort() == null;
    }

    @Override
    public int hashCode() {
        int result = offset;
        result = 31 * result + size;
        result = 31 * result;
        return result;
    }

    @Override
    public String toString() {
        return "Page request [number: " + getPageNumber() + ", size " + getPageSize() + ", sort: " + null + "]";
    }
}
