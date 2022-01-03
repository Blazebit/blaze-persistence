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

package com.blazebit.persistence.view;

import com.blazebit.persistence.ParameterHolder;

import java.util.Map;

/**
 * A factory for creating a {@link CorrelationProvider}.
 * 
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface CorrelationProviderFactory {

    /**
     * Returns whether the {@link CorrelationProvider} is parameterized or not.
     *
     * @return whether the {@link CorrelationProvider} is parameterized or not
     */
    public boolean isParameterized();

    /**
     * Creates and returns a new correlation provider for the given parameters.
     *
     * @param parameterHolder The parameter holder i.e. a {@link com.blazebit.persistence.CriteriaBuilder}
     * @param optionalParameters The optional parameter map
     * @return the correlation provider
     */
    public CorrelationProvider create(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters);
}
