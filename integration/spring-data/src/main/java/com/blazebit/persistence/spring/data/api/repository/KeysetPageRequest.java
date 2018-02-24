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

package com.blazebit.persistence.spring.data.api.repository;

import com.blazebit.persistence.KeysetPage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Like {@link org.springframework.data.domain.PageRequest} but with support for keyset pagination.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class KeysetPageRequest implements KeysetPageable {

    private final KeysetPage keysetPage;
    private final Sort sort;
    private final int page;
    private final int pageSize;

    /**
     * Construct a page request with an optional keyset page that may be used for keyset pagination.
     *
     * @param keysetPage The keyset page
     * @param sort       The sort specification
     * @param page       The page number, 0-based
     * @param pageSize   The number of elements per page
     */
    public KeysetPageRequest(KeysetPage keysetPage, Sort sort, int page, int pageSize) {
        this.keysetPage = keysetPage;
        this.sort = sort;
        this.page = page;
        this.pageSize = pageSize;
    }

    /**
     * Construct a page request from a pageable with an optional keyset page that may be used for keyset pagination.
     *
     * @param keysetPage The keyset page
     * @param pageable   The pageable
     */
    public KeysetPageRequest(KeysetPage keysetPage, Pageable pageable) {
        this.keysetPage = keysetPage;
        this.sort = pageable.getSort();
        this.page = pageable.getPageNumber();
        this.pageSize = pageable.getPageSize();
    }

    /**
     * Construct a page request representing the current page via a keyset page and a sort specification.
     *
     * @param keysetPage The keyset page
     * @param sort       The sort specification
     */
    public KeysetPageRequest(KeysetPage keysetPage, Sort sort) {
        this.keysetPage = keysetPage;
        this.sort = sort;
        if (keysetPage.getFirstResult() == 0) {
            this.page = 0;
        } else {
            this.page = keysetPage.getFirstResult() / keysetPage.getMaxResults();
        }
        this.pageSize = keysetPage.getMaxResults();
    }

    @Override
    public KeysetPage getKeysetPage() {
        return keysetPage;
    }

    @Override
    public int getPageNumber() {
        return page;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public int getOffset() {
        return page * pageSize;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new KeysetPageRequest(keysetPage, sort, page + 1, pageSize);
    }

    @Override
    public Pageable previousOrFirst() {
        if (page == 0) {
            return this;
        }
        return new KeysetPageRequest(keysetPage, sort, page - 1, pageSize);
    }

    @Override
    public Pageable first() {
        if (page == 0) {
            return this;
        }
        return new KeysetPageRequest(keysetPage, sort, 0, pageSize);
    }

    @Override
    public boolean hasPrevious() {
        return page > 0;
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

        if (page != that.getPageNumber()) {
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
        result = 31 * result + getPageNumber();
        result = 31 * result + getPageSize();
        return result;
    }
}
