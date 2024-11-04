/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webmvc.repository;

import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.IdClassEntity;
import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.IdClassEntityId;
import org.springframework.data.repository.Repository;

/**
 * @author Christian Beikov
 * @since 1.6.8
 */
public interface IdClassEntityRepository extends Repository<IdClassEntity, IdClassEntityId> {
}
