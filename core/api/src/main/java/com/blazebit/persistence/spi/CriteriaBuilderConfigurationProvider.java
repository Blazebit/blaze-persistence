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

package com.blazebit.persistence.spi;

/**
 * Interface implemented by the criteria provider.
 *
 * Implementations are instantiated via {@link java.util.ServiceLoader}.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface CriteriaBuilderConfigurationProvider {

    /**
     * Creates and returns a new criteria builder configuration.
     *
     * @return A new criteria builder configuration
     */
    public CriteriaBuilderConfiguration createConfiguration();

    /**
     * Creates and returns a new criteria builder configuration.
     *
     * @param packageOpener The package opener to use to obtain access to user classes
     * @return A new criteria builder configuration
     * @since 1.2.0
     */
    public CriteriaBuilderConfiguration createConfiguration(PackageOpener packageOpener);
}
