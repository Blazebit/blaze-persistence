/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webflux.repository;

import com.blazebit.persistence.spring.data.testsuite.webflux.view.PersonUpdateView;
import com.blazebit.persistence.view.EntityViewManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@Component
@Transactional
public class ModificationPersonRepositoryImpl implements ModificationPersonRepository {
    @Autowired
    private EntityManager em;
    @Autowired
    private EntityViewManager evm;

    @Override
    public void updatePerson(PersonUpdateView personUpdateView) {
        evm.save(em, personUpdateView);
    }
}
