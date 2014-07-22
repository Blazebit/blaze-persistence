/*
 * Copyright 2014 Blazebit.
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

import com.blazebit.persistence.CriteriaBuilder;
import java.util.Iterator;
import java.util.ServiceLoader;
import javax.persistence.EntityManager;

/**
 * Bootstrap class that is used to obtain a {@linkplain CriteriaBuilder} instance.
 *
 * @author Christian Beikov
 */
public class Criteria {

    /**
     * Returns the first {@linkplain CriteriaProvider} that is found
     *
     * @return The first {@linkplain CriteriaProvider} that is found
     */
    public static CriteriaProvider getDefaultProvider() {
        ServiceLoader<CriteriaProvider> serviceLoader = ServiceLoader.load(CriteriaProvider.class);
        Iterator<CriteriaProvider> iterator = serviceLoader.iterator();

        if (iterator.hasNext()) {
            return iterator.next();
        }

        throw new IllegalStateException(
            "No CriteriaProvider found on the class path. Please check if a valid implementation is on the class path.");
    }

    /**
     * Uses the default {@linkplain CriteriaProvider} and invokes {@link CriteriaProvider#from(javax.persistence.EntityManager, java.lang.Class) }.
     *
     * @param <T>   The query result type
     * @param em    The entity manager that should be used for the query
     * @param clazz The from class
     * @return A criteria builder for the given from class
     */
    public static <T> CriteriaBuilder<T> from(EntityManager em, Class<T> clazz) {
        return getDefaultProvider()
            .from(em, clazz);
    }

    /**
     * Uses the default {@linkplain CriteriaProvider} and invokes {@link CriteriaProvider#from(javax.persistence.EntityManager, java.lang.Class, java.lang.String) }.
     *
     * @param <T>   The query result type
     * @param em    The entity manager that should be used for the query
     * @param clazz The from class
     * @param alias The alias for the from class
     * @return A criteria builder for the given from class
     */
    public static <T> CriteriaBuilder<T> from(EntityManager em, Class<T> clazz, String alias) {
        return getDefaultProvider()
            .from(em, clazz, alias);
    }
}
