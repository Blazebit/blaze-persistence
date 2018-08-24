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

package com.blazebit.persistence.spring.data.testsuite.accessor;

import com.blazebit.persistence.spring.data.testsuite.entity.Document;
import com.blazebit.persistence.spring.data.base.query.KeysetAwarePageImpl;
import com.blazebit.persistence.spring.data.testsuite.view.DocumentView;
import com.blazebit.persistence.spring.data.repository.KeysetAwarePage;
import com.blazebit.persistence.spring.data.repository.KeysetPageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

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
        return new PageImpl<>(of(page.getContent()), new PageRequest(page.getNumber(), page.getSize()), page.getTotalElements());
    }

    public static KeysetAwarePage<DocumentAccessor> of(KeysetAwarePage<?> page) {
        return new KeysetAwarePageImpl<>(of(page.getContent()), (int) page.getTotalElements(), page.getKeysetPage(), new KeysetPageRequest(page.getKeysetPage(), page.getSort(), page.getNumber() * page.getSize(), page.getSize()));
    }

    public static Slice<DocumentAccessor> of(Slice<?> slice) {
        return new SliceImpl<>(of(slice.getContent()), new PageRequest(slice.getNumber() * slice.getSize(), slice.getSize()), slice.hasNext());
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

        @Override
        public Long getOwnerId() {
            return document.getOwner().getId();
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

        @Override
        public Long getOwnerId() {
            return documentView.getOwner().getId();
        }
    }


}
