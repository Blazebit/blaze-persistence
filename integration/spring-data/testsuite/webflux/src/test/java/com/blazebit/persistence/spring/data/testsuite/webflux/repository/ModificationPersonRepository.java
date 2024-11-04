/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webflux.repository;

import com.blazebit.persistence.spring.data.testsuite.webflux.view.PersonUpdateView;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public interface ModificationPersonRepository {

    void updatePerson(PersonUpdateView personUpdateView);
}
