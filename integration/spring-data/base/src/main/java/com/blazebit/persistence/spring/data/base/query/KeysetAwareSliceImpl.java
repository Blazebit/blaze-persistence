/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.base.query;

import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.spring.data.repository.KeysetAwareSlice;
import com.blazebit.persistence.spring.data.repository.KeysetPageRequest;
import com.blazebit.persistence.spring.data.repository.KeysetPageable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class KeysetAwareSliceImpl<T> extends SliceImpl<T> implements KeysetAwareSlice<T> {

    private final KeysetPage keysetPage;

    public KeysetAwareSliceImpl(List<T> list) {
        super(list);
        this.keysetPage = null;
    }

    public KeysetAwareSliceImpl(PagedList<T> list, Pageable pageable) {
        super(list.size() > pageable.getPageSize() ? list.subList(0, pageable.getPageSize()) : list, keysetPageable(list.getKeysetPage(), pageable), list.size() > pageable.getPageSize());
        this.keysetPage = list.getKeysetPage();
    }

    public KeysetAwareSliceImpl(List<T> list, KeysetPage keysetPage, Pageable pageable) {
        super(list.size() > pageable.getPageSize() ? list.subList(0, pageable.getPageSize()) : list, keysetPageable(keysetPage, pageable), list.size() > pageable.getPageSize());
        this.keysetPage = keysetPage;
    }

    @Override
    public KeysetPage getKeysetPage() {
        return keysetPage;
    }

    @Override
    public KeysetPageable nextPageable() {
        return (KeysetPageable) super.nextPageable();
    }

    @Override
    public KeysetPageable previousPageable() {
        return (KeysetPageable) super.previousPageable();
    }

    private static Pageable keysetPageable(KeysetPage keysetPage, Pageable pageable) {
        if (pageable instanceof KeysetPageRequest) {
            if (keysetPage == null || ((KeysetPageRequest) pageable).getKeysetPage() == keysetPage) {
                return pageable;
            }
        }

        if (keysetPage == null) {
            if (pageable instanceof KeysetPageable) {
                return new KeysetPageRequest(null, pageable.getSort(), ((KeysetPageable) pageable).getIntOffset(), pageable.getPageSize());
            } else {
                return new KeysetPageRequest(pageable.getPageNumber(), pageable.getPageSize(), null, pageable.getSort());
            }
        } else {
            return new KeysetPageRequest(keysetPage, pageable.getSort());
        }
    }

}
