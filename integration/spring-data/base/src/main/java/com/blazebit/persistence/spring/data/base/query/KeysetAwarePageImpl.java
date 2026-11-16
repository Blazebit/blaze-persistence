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
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class KeysetAwarePageImpl<T> extends PageImpl<T> implements KeysetAwarePage<T> {

    private final KeysetPage keysetPage;
    // Spring Data 1.x PageImpl has no getPageable()
    private final Pageable pageable;

    public KeysetAwarePageImpl(List<T> list) {
        super(list);
        this.keysetPage = null;
        this.pageable = null;
    }

    public KeysetAwarePageImpl(PagedList<T> list, Pageable pageable) {
        super(list, keysetPageable(list.getKeysetPage(), pageable), list.getTotalSize());
        this.keysetPage = list.getKeysetPage();
        this.pageable = keysetPageable(keysetPage, pageable);
    }

    public KeysetAwarePageImpl(List<T> list, long totalSize, KeysetPage keysetPage, Pageable pageable) {
        super(list, keysetPageable(keysetPage, pageable), totalSize);
        this.keysetPage = keysetPage;
        this.pageable = keysetPageable(keysetPage, pageable);
    }

    @Override
    public KeysetPage getKeysetPage() {
        return keysetPage;
    }

    // Spring Data 1.x declares map(Converter) whereas 2.x+ declares map(Function). Since this class is compiled against both,
    // we provide both overloads without @Override and avoid getConvertedContent() which also differs in its parameter type.
    public <U> Page<U> map(Function<? super T, ? extends U> converter) {
        List<U> content = new ArrayList<>(getContent().size());
        for (T element : getContent()) {
            content.add(converter.apply(element));
        }
        if (pageable == null) {
            return new KeysetAwarePageImpl<U>(content);
        }
        return new KeysetAwarePageImpl<U>(content, getTotalElements(), keysetPage, pageable);
    }

    public <U> Page<U> map(final Converter<? super T, ? extends U> converter) {
        return map(new Function<T, U>() {
            @Override
            public U apply(T element) {
                return converter.convert(element);
            }
        });
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
