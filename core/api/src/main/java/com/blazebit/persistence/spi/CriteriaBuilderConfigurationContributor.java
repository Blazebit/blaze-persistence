/*
 * Copyright 2014 - 2022 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.spi;

/**
 * A bootstrap process hook for contributing settings to the {@link CriteriaBuilderConfiguration}.
 * {@code CriteriaBuilderConfigurationContributor} instances may be annotated with {@link Priority}
 * (or {@code javax.annotation.Priority}) to influence the order in which they are registered.
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
