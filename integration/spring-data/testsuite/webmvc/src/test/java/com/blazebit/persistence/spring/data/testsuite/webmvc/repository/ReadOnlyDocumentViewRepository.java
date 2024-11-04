/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webmvc.repository;

import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.Document;
import com.blazebit.persistence.spring.data.testsuite.webmvc.view.DocumentView;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@Repository
@Transactional(readOnly = true)
public interface ReadOnlyDocumentViewRepository extends ReadOnlyDocumentRepository<DocumentView> {
    List<Document> findByDescription(String description);
}
