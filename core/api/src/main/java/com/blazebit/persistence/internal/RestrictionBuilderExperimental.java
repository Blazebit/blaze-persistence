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

package com.blazebit.persistence.internal;

import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SubqueryInitiator;

/**
 * This interface contains experimental restriction builder methods.
 *
 * @param <T> The builder type that is returned on terminal operations
 * @author Moritz Becker
 * @since 1.0
 */
public interface RestrictionBuilderExperimental<T> extends RestrictionBuilder<T> {

    /**
     * Starts a {@link SubqueryInitiator} for the right hand side of the IN predicate.
     * 
     * <p>
     * All occurrences of
     * <code>subqueryAlias</code> in <code>expression</code> will be replaced by the subquery.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type {@linkplain T}.
     * </p>
     * 
     * <p>
     * NOTE: This does not conform to the JPQL 2.1 specification
     * </p>
     * 
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression    The expression which will be added as select item.
     * This expression contains the {@code subqueryAlias} to define the insertion points for the subquery.
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<T> in(String subqueryAlias, String expression);
    
    /**
     * Starts a {@link SubqueryInitiator} for the right hand side of the IN predicate.
     * 
     * <p>
     * All occurrences of
     * <code>subqueryAlias</code> in <code>expression</code> will be replaced by the subquery.
     * When the builder finishes, the predicate is added to the parent predicate container represented by the type {@linkplain T}.
     * </p>
     * 
     * <p>
     * NOTE: This does not conform to the JPQL 2.1 specification
     * </p>
     * 
     * @param subqueryAlias The alias for the subquery which will be replaced by the actual subquery
     * @param expression    The expression which will be added as select item.
     * This expression contains the {@code subqueryAlias} to define the insertion points for the subquery.
     * @return The subquery initiator for building a subquery
     */
    public SubqueryInitiator<T> notIn(String subqueryAlias, String expression);
}
