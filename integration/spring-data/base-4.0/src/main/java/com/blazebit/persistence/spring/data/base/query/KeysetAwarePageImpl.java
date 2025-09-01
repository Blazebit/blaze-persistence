/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.base.query;

import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.spring.data.repository.KeysetAwarePage;
import com.blazebit.persistence.spring.data.repository.KeysetPageRequest;
import com.blazebit.persistence.spring.data.repository.KeysetPageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @author Christian Beikov
 * @author Eugen Mayer
 * @since 1.6.9
 */
public class KeysetAwarePageImpl<T> extends PageImpl<T> implements KeysetAwarePage<T> {

    private final KeysetPage keysetPage;

    public KeysetAwarePageImpl(List<T> list) {
        super(list);
        this.keysetPage = null;
    }

    public KeysetAwarePageImpl(PagedList<T> list, Pageable pageable) {
        super(list, keysetPageable(list.getKeysetPage(), pageable), list.getTotalSize());
        this.keysetPage = list.getKeysetPage();
    }

    public KeysetAwarePageImpl(List<T> list, long totalSize, KeysetPage keysetPage, Pageable pageable) {
        super(list, keysetPageable(keysetPage, pageable), totalSize);
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
