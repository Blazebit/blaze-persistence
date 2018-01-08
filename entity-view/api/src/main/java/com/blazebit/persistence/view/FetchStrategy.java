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
 * The fetch strategy for an entity view attribute.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public enum FetchStrategy {

    /**
     * A strategy that defines that the target elements are joined and fetched along in the source query.
     */
    JOIN,
    /**
     * A strategy that defines that the target elements are selected in separate queries. Depending on the defined {@link BatchFetch#size()}, the query select multiple elements at once.
     */
    SELECT,
    /**
     * A strategy that defines that the target elements are selected in a single query containing the source query as subquery.
     */
    SUBSELECT;
}
