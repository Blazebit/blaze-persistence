/*
 * Copyright 2014 - 2023 Blazebit.
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

import com.blazebit.persistence.spi.ServiceProvider;

/**
 * An interface for builders that support CTEs.
 * This is related to the fact, that a query builder supports the with clause.
 *
 * @param <T> The concrete builder type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface CTEBuilder<T extends CTEBuilder<T>> extends ServiceProvider {

    /**
     * Creates a builder for a CTE with the given CTE type.
     * 
     * @param cteClass The type of the CTE
     * @return The CTE builder
     */
    public FullSelectCTECriteriaBuilder<T> with(Class<?> cteClass);

    /**
     * Creates a builder for a CTE with the given CTE type with the given criteria builder as basis.
     *
     * @param cteClass The type of the CTE
     * @param criteriaBuilder The criteria builder to copy the query from
     * @return The CTE builder
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<T> with(Class<?> cteClass, CriteriaBuilder<?> criteriaBuilder);

    /**
     * Like {@link #with(Class)} but with the option to define whether the query should be inlined.
     *
     * @param cteClass The type of the CTE
     * @param inline Whether to inline the query defined by the CTE
     * @return The CTE builder
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<T> with(Class<?> cteClass, boolean inline);

    /**
     * Like {@link #with(Class, CriteriaBuilder)} but with the option to define whether the query should be inlined.
     *
     * @param cteClass The type of the CTE
     * @param criteriaBuilder The criteria builder to copy the query from
     * @param inline Whether to inline the query defined by the CTE
     * @return The CTE builder
     * @since 1.4.1
     */
    public FullSelectCTECriteriaBuilder<T> with(Class<?> cteClass, CriteriaBuilder<?> criteriaBuilder, boolean inline);

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
     * Like {@link #withStartSet(Class)} but with the option to define whether the query should be inlined.
     *
     * @param cteClass The type of the CTE
     * @param inline Whether to inline the query defined by the CTE
     * @return The CTE set operation builder
     * @since 1.4.1
     */
    public StartOngoingSetOperationCTECriteriaBuilder<T, LeafOngoingFinalSetOperationCTECriteriaBuilder<T>> withStartSet(Class<?> cteClass, boolean inline);

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

    /**
     * Returns whether a CTE for the given type is defined.
     *
     * @param cte The type of the CTE to check
     * @return true when a CTE for the given type is defined
     * @since 1.4.0
     */
    public boolean hasCte(Class<?> cte);

}
