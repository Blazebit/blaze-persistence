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

package com.blazebit.persistence.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.2.0
 */
public abstract class AbstractQueryConfiguration implements QueryConfiguration {

    @Override
    public String getProperty(String name) {
        switch (name) {
            case ConfigurationProperties.COMPATIBLE_MODE: return Boolean.toString(isCompatibleModeEnabled());
            case ConfigurationProperties.RETURNING_CLAUSE_CASE_SENSITIVE: return Boolean.toString(isReturningClauseCaseSensitive());
            case ConfigurationProperties.SIZE_TO_COUNT_TRANSFORMATION: return Boolean.toString(isCountTransformationEnabled());
            case ConfigurationProperties.IMPLICIT_GROUP_BY_FROM_SELECT: return Boolean.toString(isImplicitGroupByFromSelectEnabled());
            case ConfigurationProperties.IMPLICIT_GROUP_BY_FROM_HAVING: return Boolean.toString(isImplicitGroupByFromHavingEnabled());
            case ConfigurationProperties.IMPLICIT_GROUP_BY_FROM_ORDER_BY: return Boolean.toString(isImplicitGroupByFromOrderByEnabled());
            case ConfigurationProperties.EXPRESSION_OPTIMIZATION: return Boolean.toString(isExpressionOptimizationEnabled());
            case ConfigurationProperties.EXPRESSION_CACHE_CLASS: return getExpressionCacheClass();
            case ConfigurationProperties.VALUES_CLAUSE_FILTER_NULLS: return Boolean.toString(isValuesClauseFilterNullsEnabled());
            case ConfigurationProperties.OPTIMIZED_KEYSET_PREDICATE_RENDERING: return Boolean.toString(isOptimizedKeysetPredicateRenderingEnabled());
            default: return null;
        }
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<>(20);
        properties.put(ConfigurationProperties.COMPATIBLE_MODE, Boolean.toString(isCompatibleModeEnabled()));
        properties.put(ConfigurationProperties.RETURNING_CLAUSE_CASE_SENSITIVE, Boolean.toString(isReturningClauseCaseSensitive()));
        properties.put(ConfigurationProperties.SIZE_TO_COUNT_TRANSFORMATION, Boolean.toString(isCountTransformationEnabled()));
        properties.put(ConfigurationProperties.IMPLICIT_GROUP_BY_FROM_SELECT, Boolean.toString(isImplicitGroupByFromSelectEnabled()));
        properties.put(ConfigurationProperties.IMPLICIT_GROUP_BY_FROM_HAVING, Boolean.toString(isImplicitGroupByFromHavingEnabled()));
        properties.put(ConfigurationProperties.IMPLICIT_GROUP_BY_FROM_ORDER_BY, Boolean.toString(isImplicitGroupByFromOrderByEnabled()));
        properties.put(ConfigurationProperties.EXPRESSION_OPTIMIZATION, Boolean.toString(isExpressionOptimizationEnabled()));
        properties.put(ConfigurationProperties.EXPRESSION_CACHE_CLASS, getExpressionCacheClass());
        properties.put(ConfigurationProperties.VALUES_CLAUSE_FILTER_NULLS, Boolean.toString(isValuesClauseFilterNullsEnabled()));
        properties.put(ConfigurationProperties.OPTIMIZED_KEYSET_PREDICATE_RENDERING, Boolean.toString(isOptimizedKeysetPredicateRenderingEnabled()));
        return properties;
    }

}
