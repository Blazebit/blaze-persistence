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

package com.blazebit.persistence.deltaspike.data.base.handler;

import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.deltaspike.data.KeysetAwarePage;
import com.blazebit.persistence.deltaspike.data.KeysetAwareSlice;
import com.blazebit.persistence.deltaspike.data.KeysetPageRequest;
import com.blazebit.persistence.deltaspike.data.KeysetPageable;
import com.blazebit.persistence.deltaspike.data.Pageable;
import com.blazebit.persistence.deltaspike.data.Sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class KeysetAwareSliceImpl<T> implements KeysetAwareSlice<T> {

    private final List<T> content = new ArrayList<>();
    private final boolean hasNext;
    private final KeysetPageable pageable;
    private final KeysetPage keysetPage;

    public KeysetAwareSliceImpl(List<T> list) {
        this(list, null, null, false);
    }

    public KeysetAwareSliceImpl(List<T> list, Pageable pageable) {
        this(list, null, pageable);
    }

    public KeysetAwareSliceImpl(PagedList<T> list, Pageable pageable) {
        this(list.size() > pageable.getPageSize() ? list.subList(0, pageable.getPageSize()) : list, list.getKeysetPage(), keysetPageable(list.getKeysetPage(), pageable), list.size() > pageable.getPageSize());
    }

    public KeysetAwareSliceImpl(List<T> list, KeysetPage keysetPage, Pageable pageable) {
        this(list.size() > pageable.getPageSize() ? list.subList(0, pageable.getPageSize()) : list, keysetPage, keysetPageable(keysetPage, pageable), list.size() > pageable.getPageSize());
    }

    public KeysetAwareSliceImpl(List<T> list, KeysetPage keysetPage, KeysetPageable pageable, boolean hasNext) {
        this.content.addAll(list);
        this.pageable = pageable;
        this.hasNext = hasNext;
        this.keysetPage = keysetPage;
    }

    @Override
    public KeysetPage getKeysetPage() {
        return keysetPage;
    }

    @Override
    public KeysetPageable currentPageable() {
        return pageable;
    }

    @Override
    public KeysetPageable nextPageable() {
        return pageable.next();
    }

    @Override
    public KeysetPageable previousPageable() {
        return pageable.previousOrFirst();
    }

    private static KeysetPageable keysetPageable(KeysetPage keysetPage, Pageable pageable) {
        if (pageable instanceof KeysetPageRequest) {
            if (keysetPage == null || ((KeysetPageRequest) pageable).getKeysetPage() == keysetPage) {
                return (KeysetPageable) pageable;
            }
        }

        if (keysetPage == null) {
            return new KeysetPageRequest(null, pageable.getSort(), pageable.getOffset(), pageable.getPageSize());
        } else {
            return new KeysetPageRequest(keysetPage, pageable.getSort());
        }
    }

    @Override
    public int getNumber() {
        return pageable == null ? 0 : pageable.getPageNumber();
    }

    @Override
    public int getSize() {
        return pageable == null ? 0 : pageable.getPageSize();
    }

    @Override
    public int getNumberOfElements() {
        return content.size();
    }

    @Override
    public boolean hasPrevious() {
        return getNumber() > 0;
    }

    @Override
    public boolean isFirst() {
        return !hasPrevious();
    }

    @Override
    public boolean isLast() {
        return !hasNext();
    }

    @Override
    public boolean hasContent() {
        return !content.isEmpty();
    }

    @Override
    public List<T> getContent() {
        return Collections.unmodifiableList(content);
    }

    @Override
    public Sort getSort() {
        return pageable == null ? null : pageable.getSort();
    }

    @Override
    public Iterator<T> iterator() {
        return content.iterator();
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof KeysetAwarePage<?>)) {
            return false;
        }

        KeysetAwarePage<?> that = (KeysetAwarePage<?>) o;

        if (!content.equals(that.getContent())) {
            return false;
        }
        if (pageable != null ? !pageable.equals(that.currentPageable()) : that.currentPageable() != null) {
            return false;
        }
        return keysetPage != null ? keysetPage.equals(that.getKeysetPage()) : that.getKeysetPage() == null;
    }

    @Override
    public int hashCode() {
        int result = content.hashCode();
        result = 31 * result + (pageable != null ? pageable.hashCode() : 0);
        result = 31 * result + (keysetPage != null ? keysetPage.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        String contentType = "UNKNOWN";
        List<T> content = getContent();

        if (content.size() > 0) {
            contentType = content.get(0).getClass().getName();
        }

        return String.format("Slice %s containing %s instances", getNumber() + 1, contentType);
    }

}
