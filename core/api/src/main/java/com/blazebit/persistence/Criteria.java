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

package com.blazebit.persistence;

import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.spi.CriteriaBuilderConfigurationProvider;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Bootstrap class that is used to obtain a {@linkplain CriteriaBuilder} instance.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class Criteria {

    private Criteria() {
    }

    /**
     * Returns the first {@linkplain CriteriaBuilderConfigurationProvider} that is found.
     *
     * @return The first {@linkplain CriteriaBuilderConfigurationProvider} that is found
     */
    public static CriteriaBuilderConfigurationProvider getDefaultProvider() {
        ServiceLoader<CriteriaBuilderConfigurationProvider> serviceLoader = ServiceLoader.load(CriteriaBuilderConfigurationProvider.class);
        Iterator<CriteriaBuilderConfigurationProvider> iterator = serviceLoader.iterator();

        if (iterator.hasNext()) {
            return iterator.next();
        }

        throw new IllegalStateException("No CriteriaBuilderConfigurationProvider found on the class path. Please check if a valid implementation is on the class path.");
    }

    /**
     * Uses the default {@linkplain CriteriaBuilderConfigurationProvider} and invokes
     * {@link CriteriaBuilderConfigurationProvider#createConfiguration() }.
     *
     * @return A new criteria builder configuration
     */
    public static CriteriaBuilderConfiguration getDefault() {
        return getDefaultProvider().createConfiguration(DefaultPackageOpener.INSTANCE);
    }
}
