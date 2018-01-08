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

import javax.persistence.EntityManager;

/**
 * A service provider factory to create {@link JpaProvider} instances.
 * Instances are created via {@link java.util.ServiceLoader} means.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface JpaProviderFactory {

    /**
     * Create a {@link JpaProvider} for the given entity manager.
     *
     * @param em The optional entity manager to use for creating the provider
     * @return The jpa provider instance
     */
    public JpaProvider createJpaProvider(EntityManager em);
}
