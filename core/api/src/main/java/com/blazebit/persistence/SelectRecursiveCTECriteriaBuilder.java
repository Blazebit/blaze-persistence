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

/**
 * A builder for CTE criteria queries. This is the entry point for building CTE queries.
 *
 * @param <X> The result type which is returned afte the CTE builder
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface SelectRecursiveCTECriteriaBuilder<X> extends SelectBaseCTECriteriaBuilder<SelectRecursiveCTECriteriaBuilder<X>> {

    /**
     * Finishes the CTE builder for the non-recursive part and starts the builder for the recursive part.
     * The union operator is used for connecting the non-recursive and recursive part, thus removing duplicates. 
     *
     * @return The parent query builder
     */
    public SelectCTECriteriaBuilder<X> union();
    
    /**
     * Finishes the CTE builder for the non-recursive part and starts the builder for the recursive part.
     * The union all operator is used for connecting the non-recursive and recursive part, thus not removing duplicates.
     *
     * @return The parent query builder
     */
    public SelectCTECriteriaBuilder<X> unionAll();
}
