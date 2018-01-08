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

package com.blazebit.persistence.view;

import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.WhereBuilder;

/**
 * An attribute filter provider is an object that applies restrictions on a {@link WhereBuilder}.
 *
 * Attribute filter providers must have a constructor that accepts either of the following parameter types
 * <ul>
 * <li>none</li>
 * <li>{@linkplain Class}</li>
 * <li>{@linkplain Object}</li>
 * <li>{@linkplain Class}, {@linkplain Object}</li>
 * </ul>
 *
 * The {@linkplain Class} argument refers to the expected type i.e. the attribute type. The object argument refers to the filter value.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AttributeFilterProvider {

    /**
     * Applies restrictions for the given attributeExpression on the given where builder.
     *
     * @param <T>                 The actual type of the where builder
     * @param whereBuilder        The where builder on which the restrictions should be applied
     * @param attributeExpression The expression for the attribute on which a restriction should be applied
     * @return The object which the restriction builder returns on a terminal operation
     */
    public <T extends WhereBuilder<T>> T apply(T whereBuilder, String attributeExpression) {
        return apply(whereBuilder.where(attributeExpression));
    }

    /**
     * Applies restrictions for the given subquery on the given where builder.
     *
     * @param <T>               The actual type of the where builder
     * @param whereBuilder      The where builder on which the restrictions should be applied
     * @param subqueryAlias     The alias for the subquery which will be replaced by the actual subquery
     * @param subqueryExpresion The expression which wraps a subquery
     * @param provider          The provider for the subquery
     * @return The object which the restriction builder returns on a terminal operation
     */
    public <T extends WhereBuilder<T>> T apply(T whereBuilder, String subqueryAlias, String subqueryExpresion, SubqueryProvider provider) {
        if (subqueryAlias == null) {
            return apply(provider.createSubquery(whereBuilder.whereSubquery()));
        } else {
            return apply(provider.createSubquery(whereBuilder.whereSubquery(subqueryAlias, subqueryExpresion)));
        }
    }

    /**
     * Applies a restriction on the given restriction builder.
     *
     * @param <T>                The return type of terminal operations of the restriction builder
     * @param restrictionBuilder The restriction builder
     * @return The object which the restriction builder returns on a terminal operation
     */
    protected <T> T apply(RestrictionBuilder<T> restrictionBuilder) {
        throw new UnsupportedOperationException("Method not implemented!");
    }
}
