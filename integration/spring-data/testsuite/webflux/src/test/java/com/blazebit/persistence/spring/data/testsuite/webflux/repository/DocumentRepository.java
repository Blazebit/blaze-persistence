/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webflux.repository;

import com.blazebit.persistence.spring.data.testsuite.webflux.view.DocumentUpdateView;
import com.blazebit.persistence.view.EntityViewManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

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
}
