/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.hateoas.webmvc.repository;

import com.blazebit.persistence.spring.data.repository.EntityViewRepository;
import com.blazebit.persistence.spring.data.repository.EntityViewSpecificationExecutor;
import com.blazebit.persistence.spring.hateoas.webmvc.view.DocumentView;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
@Repository
@Transactional(readOnly = true)
public interface ReadOnlyDocumentViewRepository extends EntityViewRepository<DocumentView, Long>, EntityViewSpecificationExecutor<DocumentView, Long> {
}
