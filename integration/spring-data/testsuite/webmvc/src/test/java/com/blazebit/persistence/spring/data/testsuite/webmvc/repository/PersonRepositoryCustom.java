/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webmvc.repository;

import com.blazebit.persistence.spring.data.testsuite.webmvc.view.PersonUpdateView;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public interface PersonRepositoryCustom {

    void updatePerson(PersonUpdateView personUpdateView);
}
