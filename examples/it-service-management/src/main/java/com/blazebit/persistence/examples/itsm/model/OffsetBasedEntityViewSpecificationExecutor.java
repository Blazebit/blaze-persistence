/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
