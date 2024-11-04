/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;

/**
 * A bootstrap process hook for contributing settings to the {@link CriteriaBuilderConfiguration}.
 * {@code CriteriaBuilderConfigurationContributor} instances may be annotated with {@link Priority}
 * (or {@code jakarta.annotation.Priority}) to influence the order in which they are registered.
 * The range 0-500 is reserved for internal uses. 500 - 1000 is reserved for libraries and 1000+
 * is for user provided contributors.
 *
 * Implementations are instantiated via {@link java.util.ServiceLoader}.
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.6.2
 */
public interface CriteriaBuilderConfigurationContributor {

    /**
     * Perform the process of contributing to the {@link CriteriaBuilderConfiguration}.
     *
     * @param criteriaBuilderConfiguration the {@link CriteriaBuilderConfiguration} to which to contribute
     */
    void contribute(CriteriaBuilderConfiguration criteriaBuilderConfiguration);

}
