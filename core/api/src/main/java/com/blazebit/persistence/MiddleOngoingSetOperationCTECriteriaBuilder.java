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
 * An interface for builders that support set operators.
 *
 * @param <T> The builder result type
 * @param <Y> The set sub-operation result type
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface MiddleOngoingSetOperationCTECriteriaBuilder<T, Y> extends OngoingSetOperationBuilder<OngoingSetOperationCTECriteriaBuilder<T, Y>, Y, StartOngoingSetOperationCTECriteriaBuilder<T, MiddleOngoingSetOperationCTECriteriaBuilder<T, Y>>> {

    /**
     * Finishes the current set operation builder and returns a final builder for ordering and limiting.
     *
     * @return The final builder for ordering and limiting
     */
    public OngoingFinalSetOperationCTECriteriaBuilder<Y> endSetWith();
}
