/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.webflux.repository;

import com.blazebit.persistence.examples.spring.data.webflux.model.Cat;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface CatJpaRepository extends JpaRepository<Cat, Long> {

}
