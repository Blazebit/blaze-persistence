/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql.spqr.repository;

import com.blazebit.persistence.integration.graphql.spqr.model.Cat;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
public interface CatJpaRepository extends JpaRepository<Cat, Long> {

}
