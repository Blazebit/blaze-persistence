/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.spring.data.testsuite.webmvc.repository;

import com.blazebit.persistence.spring.data.repository.EntityViewRepository;
import com.blazebit.persistence.spring.data.testsuite.webmvc.projection.DocumentIdProjection;
import com.blazebit.persistence.spring.data.testsuite.webmvc.view.DocumentView;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Moritz Becker
 * @since 1.6.0
 */
@Repository
public interface DocumentProjectionEntityViewRepository extends EntityViewRepository<DocumentView, Long> {

    List<DocumentIdProjection> findByName(String name);

    <T> List<T> findByName(String name, Class<T> type);
}
