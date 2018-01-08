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
 * @param <X> The concrete builder type
 * @param <Y> The set sub-operation result type
 * @param <Z> The set nesting start type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface StartOngoingSetOperationBuilder<X, Y, Z extends StartOngoingSetOperationBuilder<?, ?, ?>> extends BaseOngoingSetOperationBuilder<X, Y, Z> {
    
    /**
     * Starts a nested set operation builder.
     * Doing this is like starting a nested query that will be connected via a set operation.
     *
     * @return The set operation builder
     * @since 1.2.0
     */
    public Z startSet();
}
