/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webmvc.repository;

import com.blazebit.persistence.spring.data.testsuite.webmvc.view.PersonUpdateView;
import com.blazebit.persistence.view.EntityViewManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

/**
 * @author Moritz Becker
 * @since 1.4.0
 */
@Transactional
public class PersonRepositoryImpl implements PersonRepositoryCustom {
    @Autowired
    private EntityManager em;
    @Autowired
    private EntityViewManager evm;

    @Override
    public void updatePerson(PersonUpdateView personUpdateView) {
        evm.save(em, personUpdateView);
    }
}
