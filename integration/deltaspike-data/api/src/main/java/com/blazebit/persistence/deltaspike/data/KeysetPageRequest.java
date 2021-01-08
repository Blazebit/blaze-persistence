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

package com.blazebit.persistence.deltaspike.data;

import com.blazebit.persistence.KeysetPage;

/**
 * Like {@link PageRequest} but with support for keyset pagination.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class KeysetPageRequest extends PageRequest implements KeysetPageable {

    private final KeysetPage keysetPage;
    private final boolean withCountQuery;
    private final boolean withExtractAllKeysets;

    /**
     * Construct a page request with an optional keyset page that may be used for keyset pagination.
     *
     * @param page       The page number, 0-based
     * @param pageSize   The number of elements per page
     * @param keysetPage The keyset page
     * @param sort       The sort specification
     */
    public KeysetPageRequest(int page, int pageSize, KeysetPage keysetPage, Sort sort) {
        this(keysetPage, sort, page * pageSize, pageSize);
    }

    /**
     * Construct a page request from a pageable with an optional keyset page that may be used for keyset pagination.
     *
     * @param keysetPage The keyset page
     * @param pageable   The pageable
     */
    public KeysetPageRequest(KeysetPage keysetPage, Pageable pageable) {
        this(keysetPage, pageable.getSort(), pageable.getOffset(), pageable.getPageSize());
    }

    /**
     * Construct a page request representing the current page via a keyset page and a sort specification.
     *
     * @param keysetPage The keyset page
     * @param sort       The sort specification
     */
    public KeysetPageRequest(KeysetPage keysetPage, Sort sort) {
        this(keysetPage, sort, keysetPage.getFirstResult(), keysetPage.getMaxResults());
    }

    /**
     * Construct a page request with an optional keyset page that may be used for keyset pagination.
     *
     * @param keysetPage The keyset page
     * @param sort       The sort specification
     * @param offset     The offset number, 0-based
     * @param pageSize   The number of elements per page
     * @since 1.3.0
     */
    public KeysetPageRequest(KeysetPage keysetPage, Sort sort, int offset, int pageSize) {
        this(keysetPage, sort, offset, pageSize, true, false);
    }

    /**
     * Construct a page request with an optional keyset page that may be used for keyset pagination and flags to enable the count query and keyset extraction.
     *
     * @param keysetPage The keyset page
     * @param sort       The sort specification
     * @param offset     The offset number, 0-based
     * @param pageSize   The number of elements per page
     * @param withCountQuery True to enable the count query
     * @param withExtractAllKeysets True to enable extraction of all keysets
     * @since 1.4.0
     */
    public KeysetPageRequest(KeysetPage keysetPage, Sort sort, int offset, int pageSize, boolean withCountQuery, boolean withExtractAllKeysets) {
        super(sort, offset, pageSize);
        this.keysetPage = keysetPage;
        this.withCountQuery = withCountQuery;
        this.withExtractAllKeysets = withExtractAllKeysets;
    }

    @Override
    public boolean isWithCountQuery() {
        return withCountQuery;
    }

    @Override
    public boolean isWithExtractAllKeysets() {
        return withExtractAllKeysets;
    }

    @Override
    public KeysetPage getKeysetPage() {
        return keysetPage;
    }

    @Override
    public KeysetPageable next() {
        return new KeysetPageRequest(keysetPage, getSort(), getOffset() + getPageSize(), getPageSize(), isWithCountQuery(), isWithExtractAllKeysets());
    }

    @Override
    public KeysetPageable previous() {
        if (getOffset() == 0) {
            return null;
        }
        return new KeysetPageRequest(keysetPage, getSort(), getOffset() - getPageSize(), getPageSize(), isWithCountQuery(), isWithExtractAllKeysets());
    }

    @Override
    public KeysetPageable previousOrFirst() {
        if (getOffset() == 0) {
            return this;
        }
        return new KeysetPageRequest(keysetPage, getSort(), getOffset() - getPageSize(), getPageSize(), isWithCountQuery(), isWithExtractAllKeysets());
    }

    @Override
    public KeysetPageable first() {
        if (getOffset() == 0) {
            return this;
        }
        return new KeysetPageRequest(keysetPage, getSort(), 0, getPageSize());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KeysetPageable)) {
            return false;
        }

        KeysetPageable that = (KeysetPageable) o;

        if (isWithCountQuery() != that.isWithCountQuery()) {
            return false;
        }
        if (isWithExtractAllKeysets() != that.isWithExtractAllKeysets()) {
            return false;
        }
        if (getOffset() != that.getOffset()) {
            return false;
        }
        if (getPageSize() != that.getPageSize()) {
            return false;
        }
        if (getKeysetPage() != null ? !getKeysetPage().equals(that.getKeysetPage()) : that.getKeysetPage() != null) {
            return false;
        }
        return getSort() != null ? getSort().equals(that.getSort()) : that.getSort() == null;
    }

    @Override
    public int hashCode() {
        int result = getKeysetPage() != null ? getKeysetPage().hashCode() : 0;
        result = 31 * result + (getSort() != null ? getSort().hashCode() : 0);
        result = 31 * result + (isWithCountQuery() ? 1 : 0);
        result = 31 * result + (isWithExtractAllKeysets() ? 1 : 0);
        result = 31 * result + getOffset();
        result = 31 * result + getPageSize();
        return result;
    }
}
