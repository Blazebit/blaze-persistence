/*
 * Copyright 2015 Blazebit.
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

/**
 * An interface for builders that support limit and offset.
 * This is related to the fact, that a query builder supports the limit and offset clause.
 *
 * @param <T> The concrete builder type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface LimitBuilder<X extends LimitBuilder<X>> {

    // TODO: documentation
    public X setFirstResult(int firstResult);

    // TODO: documentation
    public X setMaxResults(int maxResults);

    // TODO: documentation
    public int getFirstResult();

    // TODO: documentation
    public int getMaxResults();
    
}
