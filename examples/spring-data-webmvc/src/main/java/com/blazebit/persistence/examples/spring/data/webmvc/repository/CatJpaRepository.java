/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.webmvc.repository;

import com.blazebit.persistence.examples.spring.data.webmvc.model.Cat;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface CatJpaRepository extends JpaRepository<Cat, Long> {

}
