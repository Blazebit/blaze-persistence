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
package com.blazebit.persistence;

/**
 * An interface for builders that support CTEs.
 * This is related to the fact, that a query builder supports the with clause.
 *
 * @param <T> The concrete builder type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface CTEBuilder<T extends CTEBuilder<T>> {

    /**
     * TODO: documentation
     * 
     * @param cteClass
     * @return
     * @since 1.1.0
     */
    public FullSelectCTECriteriaBuilder<T> with(Class<?> cteClass);

    // TODO: documentation
    public StartOngoingSetOperationCTECriteriaBuilder<T, LeafOngoingSetOperationCTECriteriaBuilder<T>> withStartSet(Class<?> cteClass);

    /**
     * TODO: documentation
     * 
     * @param cteClass
     * @return
     * @since 1.1.0
     */
    public SelectRecursiveCTECriteriaBuilder< T> withRecursive(Class<?> cteClass);

    /**
     * TODO: documentation
     * 
     * @param cteClass
     * @return
     * @since 1.1.0
     */
    public ReturningModificationCriteriaBuilderFactory<T> withReturning(Class<?> cteClass);

}
