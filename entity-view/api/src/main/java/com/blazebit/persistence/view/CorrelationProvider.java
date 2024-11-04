/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

/**
 * Provides correlation functionality for entity views.
 * Beware that correlation providers may only be used once for an entity view hierarchy.
 * Also note that aliases defined in the query builder will contribute to the main query
 * when using {@link FetchStrategy#JOIN}, so be careful and choose rather unique names.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface CorrelationProvider {

    /**
     * Applies a correlation to a query builder.
     * Depending on the correlation strategy, the <i>correlationExpression</i> may be one of the following:
     * <ul>
     *     <li>{@link FetchStrategy#SELECT} - A named parameter or correlation expression if batched</li>
     *     <li>{@link FetchStrategy#SUBSELECT} - The correlation expression</li>
     *     <li>{@link FetchStrategy#JOIN} - The correlation expression</li>
     *     <li>{@link FetchStrategy#MULTISET} - The correlation expression</li>
     * </ul>
     *
     * To be able to make use of all strategies it is best if you use the IN predicate in conjunction with the <i>correlationExpression</i>.
     *
     * @param correlationBuilder    The correlation builder to create the correlation
     * @param correlationExpression The correlation expression from the outer query on which to correlate
     */
    public void applyCorrelation(CorrelationBuilder correlationBuilder, String correlationExpression);
}
