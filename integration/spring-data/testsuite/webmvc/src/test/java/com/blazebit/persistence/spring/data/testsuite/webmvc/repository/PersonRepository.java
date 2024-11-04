/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webmvc.repository;

import com.blazebit.persistence.spring.data.repository.EntityViewRepository;
import com.blazebit.persistence.spring.data.testsuite.webmvc.view.PersonView;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@Transactional(readOnly = true)
public interface PersonRepository extends EntityViewRepository<PersonView, String>, PersonRepositoryCustom {
}
