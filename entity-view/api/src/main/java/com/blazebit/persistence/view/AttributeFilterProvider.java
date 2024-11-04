/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
 * @param <FilterValue> The filter value type
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AttributeFilterProvider<FilterValue> {

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
