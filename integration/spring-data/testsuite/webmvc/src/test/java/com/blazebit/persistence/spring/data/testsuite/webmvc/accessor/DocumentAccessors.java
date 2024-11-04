/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webmvc.accessor;

import com.blazebit.persistence.spring.data.base.query.KeysetAwarePageImpl;
import com.blazebit.persistence.spring.data.repository.KeysetAwarePage;
import com.blazebit.persistence.spring.data.repository.KeysetPageRequest;
import com.blazebit.persistence.spring.data.testsuite.webmvc.PageRequest;
import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.Document;
import com.blazebit.persistence.spring.data.testsuite.webmvc.view.DocumentView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class DocumentAccessors {

    public static DocumentAccessor of(Object o) {
        if (o instanceof Optional) {
            o = ((Optional) o).get();
        }
        if (o instanceof Document) {
            return new DocumentEntityAccessor((Document) o);
        } else if (o instanceof DocumentView) {
            return new DocumentViewAccessor((DocumentView) o);
        } else {
            throw new IllegalArgumentException("There is no accessor for the supplied instance of type [" + o.getClass() + "]");
        }
    }

    public static List<DocumentAccessor> of(Iterable<?> iterable) {
        List<DocumentAccessor> accessors = new ArrayList<>();
        for (Object o : iterable) {
            accessors.add(of(o));
        }
        return accessors;
    }

    public static Page<DocumentAccessor> of(Page<?> page) {
        return new PageImpl<>(of(page.getContent()), getPageable(page), page.getTotalElements());
    }

    private static Pageable getPageable(Page<?> page) {
        try {
            return (Pageable) Class.forName("org.springframework.data.domain.Slice").getMethod("getPageable")
                .invoke(page);
        } catch (Exception e) {
            // Ignore
        }
        if (page.getSize() < 1) {
            return null;
        }
        return new PageRequest(page.getNumber(), page.getSize());
    }

    public static KeysetAwarePage<DocumentAccessor> of(KeysetAwarePage<?> page) {
        KeysetPageRequest keysetPageRequest;
        if (getPageable(page) == unpaged()) {
            keysetPageRequest = new KeysetPageRequest(page.getKeysetPage(), page.getSort(), 0, page.getSize());
        }
        else {
            keysetPageRequest = new KeysetPageRequest(page.getKeysetPage(), page.getSort(), page.getNumber() * page.getSize(), page.getSize());
        }
        return new KeysetAwarePageImpl<>(of(page.getContent()), (int) page.getTotalElements(), page.getKeysetPage(), keysetPageRequest);
    }

    public static Slice<DocumentAccessor> of(Slice<?> slice) {
        return new SliceImpl<>(of(slice.getContent()), new PageRequest(slice.getNumber() * slice.getSize(), slice.getSize()), slice.hasNext());
    }

    private static Pageable unpaged() {
        try {
            Method unpaged = Class.forName("org.springframework.data.domain.Pageable").getMethod("unpaged");
            return (Pageable) unpaged.invoke(null);
        } catch (Exception e) {
            return null;
        }
    }

    static class DocumentEntityAccessor implements DocumentAccessor {

        private final Document document;

        public DocumentEntityAccessor(Document document) {
            this.document = document;
        }


        @Override
        public Long getId() {
            return document.getId();
        }

        @Override
        public String getName() {
            return document.getName();
        }

        @Override
        public String getDescription() {
            return document.getDescription();
        }

        @Override
        public long getAge() {
            return document.getAge();
        }

    }

    static class DocumentViewAccessor implements DocumentAccessor {

        private final DocumentView documentView;

        public DocumentViewAccessor(DocumentView documentView) {
            this.documentView = documentView;
        }

        @Override
        public Long getId() {
            return documentView.getId();
        }

        @Override
        public String getName() {
            return documentView.getName();
        }

        @Override
        public String getDescription() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getAge() {
            throw new UnsupportedOperationException();
        }

    }

}
