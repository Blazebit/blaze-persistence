/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.criteria.spi;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.criteria.BlazeCriteriaBuilder;

/**
 * A service provider for {@link com.blazebit.persistence.criteria.BlazeCriteriaBuilder} instances.
 *
 * @author Christian Beikov
 * @since 1.2.1
 */
public interface BlazeCriteriaBuilderFactory {

    /**
     * Creates a new {@link BlazeCriteriaBuilder} instance bound to the given criteria builder factory.
     *
     * @param criteriaBuilderFactory The criteria builder factory to which the persistence unit is bound
     * @return A new {@link BlazeCriteriaBuilder}
     */
    public BlazeCriteriaBuilder createCriteriaBuilder(CriteriaBuilderFactory criteriaBuilderFactory);

}
