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
     *     <li>{@link FetchStrategy#SELECT} - A named parameter</li>
     *     <li>{@link FetchStrategy#SUBSELECT} - A named collection parameter</li>
     *     <li>{@link FetchStrategy#JOIN} - The correlation expression</li>
     * </ul>
     *
     * To be able to make use of all strategies it is best if you use the IN predicate in conjunction with the <i>correlationExpression</i>.
     *
     * @param correlationBuilder    The correlation builder to create the correlation
     * @param correlationExpression The correlation expression from the outer query on which to correlate
     */
    public void applyCorrelation(CorrelationBuilder correlationBuilder, String correlationExpression);
}
