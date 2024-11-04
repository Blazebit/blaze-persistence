/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webmvc.repository;

import com.blazebit.persistence.spring.data.testsuite.webmvc.view.DocumentCreateOrUpdateView;
import com.blazebit.persistence.spring.data.testsuite.webmvc.view.DocumentUpdateView;
import com.blazebit.persistence.view.EntityViewManager;
import javax.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@Repository
@Transactional
public class DocumentRepository {

    private final EntityManager em;
    private final EntityViewManager evm;

    public DocumentRepository(EntityManager em, EntityViewManager evm) {
        this.em = em;
        this.evm = evm;
    }

    public void updateDocument(DocumentUpdateView documentUpdateView) {
        evm.save(em, documentUpdateView);
    }

    public void createDocument(DocumentCreateOrUpdateView documentCreateView) {
        evm.save(em, documentCreateView);
    }
}
