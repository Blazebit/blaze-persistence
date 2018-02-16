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

import java.util.Map;

/**
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.2.0
 */
public class MutableQueryConfiguration extends AbstractQueryConfiguration {

    private final boolean compatibleModeEnabled;
    private final boolean expressionOptimizationEnabled;
    private final String expressionCacheClass;

    private boolean returningClauseCaseSensitive;
    private boolean sizeToCountTransformationEnabled;
    private boolean implicitGroupByFromSelectEnabled;
    private boolean implicitGroupByFromHavingEnabled;
    private boolean implicitGroupByFromOrderByEnabled;
    private boolean valuesClauseFilterNullsEnabled;
    private boolean parameterAsLiteralRenderingEnabled;
    private boolean optimizedKeysetPredicateRenderingEnabled;
    private boolean cacheable;

    public MutableQueryConfiguration(QueryConfiguration queryConfiguration) {
        this.compatibleModeEnabled = queryConfiguration.isCompatibleModeEnabled();
        this.expressionOptimizationEnabled = queryConfiguration.isExpressionOptimizationEnabled();
        this.expressionCacheClass = queryConfiguration.getExpressionCacheClass();
        this.returningClauseCaseSensitive = queryConfiguration.isReturningClauseCaseSensitive();
        this.sizeToCountTransformationEnabled = queryConfiguration.isCountTransformationEnabled();
        this.implicitGroupByFromSelectEnabled = queryConfiguration.isImplicitGroupByFromSelectEnabled();
        this.implicitGroupByFromHavingEnabled = queryConfiguration.isImplicitGroupByFromHavingEnabled();
        this.implicitGroupByFromOrderByEnabled = queryConfiguration.isImplicitGroupByFromOrderByEnabled();
        this.valuesClauseFilterNullsEnabled = queryConfiguration.isValuesClauseFilterNullsEnabled();
        this.parameterAsLiteralRenderingEnabled = queryConfiguration.isParameterAsLiteralRenderingEnabled();
        this.optimizedKeysetPredicateRenderingEnabled = queryConfiguration.isOptimizedKeysetPredicateRenderingEnabled();
        this.cacheable = queryConfiguration.isCacheable();
    }

    @Override
    public boolean isCompatibleModeEnabled() {
        return compatibleModeEnabled;
    }

    @Override
    public boolean isReturningClauseCaseSensitive() {
        return returningClauseCaseSensitive;
    }

    @Override
    public boolean isExpressionOptimizationEnabled() {
        return expressionOptimizationEnabled;
    }

    @Override
    public String getExpressionCacheClass() {
        return expressionCacheClass;
    }

    @Override
    public boolean isCountTransformationEnabled() {
        return sizeToCountTransformationEnabled;
    }

    @Override
    public boolean isImplicitGroupByFromSelectEnabled() {
        return implicitGroupByFromSelectEnabled;
    }

    @Override
    public boolean isImplicitGroupByFromHavingEnabled() {
        return implicitGroupByFromHavingEnabled;
    }

    @Override
    public boolean isImplicitGroupByFromOrderByEnabled() {
        return implicitGroupByFromOrderByEnabled;
    }

    @Override
    public boolean isValuesClauseFilterNullsEnabled() {
        return valuesClauseFilterNullsEnabled;
    }

    @Override
    public boolean isParameterAsLiteralRenderingEnabled() {
        return parameterAsLiteralRenderingEnabled;
    }

    @Override
    public boolean isOptimizedKeysetPredicateRenderingEnabled() {
        return optimizedKeysetPredicateRenderingEnabled;
    }

    @Override
    public void setCacheable(boolean cacheable) {
        this.cacheable = cacheable;
    }

    @Override
    public boolean isCacheable() {
        return cacheable;
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            setProperty(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void setProperty(String propertyName, String propertyValue) {
        // CHECKSTYLE:OFF: OneStatementPerLine
        switch (propertyName) {
            case ConfigurationProperties.COMPATIBLE_MODE:                       throw propertySetNotAllowed(propertyName);
            case ConfigurationProperties.RETURNING_CLAUSE_CASE_SENSITIVE:       returningClauseCaseSensitive = booleanOrFail(propertyName, propertyValue); break;
            case ConfigurationProperties.SIZE_TO_COUNT_TRANSFORMATION:          sizeToCountTransformationEnabled = booleanOrFail(propertyName, propertyValue); break;
            case ConfigurationProperties.IMPLICIT_GROUP_BY_FROM_SELECT:         implicitGroupByFromSelectEnabled = booleanOrFail(propertyName, propertyValue); break;
            case ConfigurationProperties.IMPLICIT_GROUP_BY_FROM_HAVING:         implicitGroupByFromHavingEnabled = booleanOrFail(propertyName, propertyValue); break;
            case ConfigurationProperties.IMPLICIT_GROUP_BY_FROM_ORDER_BY:       implicitGroupByFromOrderByEnabled = booleanOrFail(propertyName, propertyValue); break;
            case ConfigurationProperties.EXPRESSION_OPTIMIZATION:               throw propertySetNotAllowed(propertyName);
            case ConfigurationProperties.EXPRESSION_CACHE_CLASS:                throw propertySetNotAllowed(propertyName);
            case ConfigurationProperties.VALUES_CLAUSE_FILTER_NULLS:            valuesClauseFilterNullsEnabled = booleanOrFail(propertyName, propertyValue); break;
            case ConfigurationProperties.PARAMETER_AS_LITERAL_RENDERING:        parameterAsLiteralRenderingEnabled = booleanOrFail(propertyName, propertyValue); break;
            case ConfigurationProperties.OPTIMIZED_KEYSET_PREDICATE_RENDERING:  optimizedKeysetPredicateRenderingEnabled = booleanOrFail(propertyName, propertyValue); break;
            default: break;
        }
        // CHECKSTYLE:ON: OneStatementPerLine
    }

    private RuntimeException propertySetNotAllowed(String propertyName) {
        return new IllegalArgumentException("Not allowed to set property: " + propertyName);
    }

    private boolean booleanOrFail(String propertyName, String propertyValue) {
        if ("true".equalsIgnoreCase(propertyValue)) {
            return true;
        } else if ("false".equalsIgnoreCase(propertyValue)) {
            return false;
        }

        throw new IllegalArgumentException("Illegal value '" + propertyValue + "' for boolean property '" + propertyName + "'");
    }
}
