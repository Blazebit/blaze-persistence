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
 * An interface for builders that support CTEs.
 * This is related to the fact, that a query builder supports the with clause.
 *
 * @param <T> The concrete builder type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface CTEBuilder<T extends CTEBuilder<T>> {

    /**
     * Creates a builder for a CTE with the given CTE type.
     * 
     * @param cteClass The type of the CTE
     * @return The CTE builder
     */
    public FullSelectCTECriteriaBuilder<T> with(Class<?> cteClass);

    /**
     * Copies the CTEs from the given CTE builder into this CTE builder.
     *
     * @param cteBuilder The CTE builder from which to copy CTEs
     * @return This for method chaining
     * @since 1.3.0
     */
    public T withCtesFrom(CTEBuilder<?> cteBuilder);

    /**
     * Creates a builder for a CTE with a nested set operation builder.
     * Doing this is like starting a nested query that will be connected via a set operation.
     *
     * @param cteClass The type of the CTE
     * @return The CTE set operation builder
     */
    public StartOngoingSetOperationCTECriteriaBuilder<T, LeafOngoingFinalSetOperationCTECriteriaBuilder<T>> withStartSet(Class<?> cteClass);

    /**
     * Creates a builder for a recursive CTE with the given CTE type.
     * 
     * @param cteClass The type of the CTE
     * @return The recursive CTE builder
     */
    public SelectRecursiveCTECriteriaBuilder< T> withRecursive(Class<?> cteClass);

    /**
     * Creates a builder for a modification CTE with the given CTE type.
     *
     * @param cteClass The type of the CTE
     * @return A factory to create a modification query that returns/binds attributes to the CTE.
     */
    public ReturningModificationCriteriaBuilderFactory<T> withReturning(Class<?> cteClass);

}
