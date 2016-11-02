/*
 * Copyright 2014 - 2016 Blazebit.
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

import com.blazebit.persistence.ObjectBuilder;
import javax.persistence.TypedQuery;

/**
 * Interface implemented by the criteria provider.
 *
 * It is invoked to transform a JPA query.
 *
 * @author Christian Beikov
 * @since 1.0
 */
public interface QueryTransformer {

    /**
     * Transforms the query.
     *
     * @param <T> The query result type
     * @param query The original query
     * @param objectBuilder The object build that should be used to transform the query
     * @return The transformed query
     */
    public <T> TypedQuery<T> transformQuery(TypedQuery<?> query, ObjectBuilder<T> objectBuilder);
}
