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

package com.blazebit.persistence.examples.itsm.model;

import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import com.blazebit.persistence.spring.data.annotation.OptionalParam;
import com.blazebit.persistence.spring.data.repository.EntityViewSpecificationExecutor;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
public interface OffsetBasedEntityViewSpecificationExecutor<V, E>
        extends EntityViewSpecificationExecutor<V, E> {

    List<V> findAll(Specification<E> specification, long offset, long limit,
            Sort sort);

    List<V> findAll(Specification<E> specification, long offset, long limit);

    @Transactional(readOnly = true)
    List<V> findAll(Specification<E> specification,
            @OptionalParam("locale") Locale locale,
            @OptionalParam("defaultLocale") Locale defaultLocale);

    @Transactional(readOnly = true)
    List<V> findAll(Specification<E> specification, Pageable pageable,
            @OptionalParam("locale") Locale locale,
            @OptionalParam("defaultLocale") Locale defaultLocale);

    @Transactional(readOnly = true)
    default List<V> findAll(Specification<E> specification, long offset,
            long limit, Sort sort, Locale locale, Locale defaultLocale) {
        OffsetBasedPageRequest pageable = OffsetBasedPageRequest.of(offset, limit, sort);
        return this.findAll(specification, pageable, locale, defaultLocale);
    }

    @Transactional(readOnly = true)
    V findOne(Specification<E> specification,
            @OptionalParam("locale") Locale locale,
            @OptionalParam("defaultLocale") Locale defaultLocale);

}
