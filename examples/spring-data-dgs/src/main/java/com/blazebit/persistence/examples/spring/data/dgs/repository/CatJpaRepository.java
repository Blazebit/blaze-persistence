/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.dgs.repository;

import com.blazebit.persistence.examples.spring.data.dgs.model.Cat;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Christian Beikov
 * @since 1.6.2
 */
public interface CatJpaRepository extends JpaRepository<Cat, Long> {

}
