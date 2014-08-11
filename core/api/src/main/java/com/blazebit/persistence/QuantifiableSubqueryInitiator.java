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
package com.blazebit.persistence;

/**
 * The interface for quantifiable subquery initiators.
 * The left hand side and the operator are already known to the initiator but a quantor can optionally be choosen.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Christian Beikov
 * @since 1.0
 */
public interface QuantifiableSubqueryInitiator<T> extends SubqueryInitiator<T> {

    /**
     * Starts a {@link SubqueryInitiator} for the right hand side of a predicate that uses the ALL quantor.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type {@linkplain T}.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<T> all();

    /**
     * Starts a {@link SubqueryInitiator} for the right hand side of a predicate that uses the ANY quantor.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type {@linkplain T}.
     *
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<T> any();
}
