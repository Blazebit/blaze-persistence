/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.repository;

import com.blazebit.persistence.CriteriaBuilder;

/**
 * @author Moritz Becker
 * @author Eugen Mayer
 * @since 1.6.9
 */
public interface BlazeSpecification {

    void applySpecification(String rootAlias, CriteriaBuilder<?> builder);
}
