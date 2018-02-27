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

package com.blazebit.persistence.spring.data.repository;

import com.blazebit.persistence.KeysetPage;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Like {@link org.springframework.data.domain.PageRequest} but with support for keyset pagination.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class KeysetPageRequest extends PageRequest implements KeysetPageable {

    private final KeysetPage keysetPage;

    /**
     * Construct a page request with an optional keyset page that may be used for keyset pagination.
     *
     * @param keysetPage The keyset page
     * @param sort       The sort specification
     * @param page       The page number, 0-based
     * @param pageSize   The number of elements per page
     */
    public KeysetPageRequest(KeysetPage keysetPage, Sort sort, int page, int pageSize) {
        super(page, pageSize, sort);
        this.keysetPage = keysetPage;
    }

    /**
     * Construct a page request from a pageable with an optional keyset page that may be used for keyset pagination.
     *
     * @param keysetPage The keyset page
     * @param pageable   The pageable
     */
    public KeysetPageRequest(KeysetPage keysetPage, Pageable pageable) {
        super(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        this.keysetPage = keysetPage;
    }

    /**
     * Construct a page request representing the current page via a keyset page and a sort specification.
     *
     * @param keysetPage The keyset page
     * @param sort       The sort specification
     */
    public KeysetPageRequest(KeysetPage keysetPage, Sort sort) {
        super(keysetPage.getFirstResult() == 0 ? 0 : keysetPage.getFirstResult() / keysetPage.getMaxResults(), keysetPage.getMaxResults(), sort);
        this.keysetPage = keysetPage;
    }

    @Override
    public KeysetPage getKeysetPage() {
        return keysetPage;
    }

    @Override
    public Pageable next() {
        return new KeysetPageRequest(keysetPage, getSort(), getPageNumber() + 1, getPageSize());
    }

    @Override
    public Pageable previousOrFirst() {
        if (getPageNumber() == 0) {
            return this;
        }
        return new KeysetPageRequest(keysetPage, getSort(), getPageNumber() - 1, getPageSize());
    }

    @Override
    public Pageable first() {
        if (getPageNumber() == 0) {
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

        if (getPageNumber() != that.getPageNumber()) {
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
