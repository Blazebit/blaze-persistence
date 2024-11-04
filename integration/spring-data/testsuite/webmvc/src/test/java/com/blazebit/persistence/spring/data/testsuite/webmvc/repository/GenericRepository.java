/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webmvc.repository;

import com.blazebit.persistence.spring.data.repository.EntityViewRepository;
import com.blazebit.persistence.spring.data.testsuite.webmvc.view.DocumentView;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
@Repository
@Transactional(readOnly = true)
public interface GenericRepository<T extends DocumentView> extends EntityViewRepository<T, Long> {

}
