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

import com.blazebit.persistence.impl.util.PropertyUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.2.0
 */
public class ImmutableQueryConfiguration extends AbstractQueryConfiguration {

    private final boolean compatibleModeEnabled;
    private final boolean expressionOptimizationEnabled;
    private final String expressionCacheClass;

    private final boolean returningClauseCaseSensitive;
    private final boolean sizeToCountTransformationEnabled;
    private final boolean implicitGroupByFromSelectEnabled;
    private final boolean implicitGroupByFromHavingEnabled;
    private final boolean implicitGroupByFromOrderByEnabled;
    private final boolean valuesClauseFilterNullsEnabled;
    private final boolean parameterAsLiteralRenderingEnabled;
    private final boolean optimizedKeysetPredicateRenderingEnabled;

    public ImmutableQueryConfiguration(Map<String, String> properties) {
        this.compatibleModeEnabled = PropertyUtils.getAsBooleanProperty(properties, ConfigurationProperties.COMPATIBLE_MODE, false);
        this.expressionOptimizationEnabled = PropertyUtils.getAsBooleanProperty(properties, ConfigurationProperties.EXPRESSION_OPTIMIZATION, true);
        this.expressionCacheClass = properties.get(ConfigurationProperties.EXPRESSION_CACHE_CLASS);

        this.returningClauseCaseSensitive =                 getBooleanProperty(properties, ConfigurationProperties.RETURNING_CLAUSE_CASE_SENSITIVE,    "false");
        this.sizeToCountTransformationEnabled =             getBooleanProperty(properties, ConfigurationProperties.SIZE_TO_COUNT_TRANSFORMATION,       "true");
        this.implicitGroupByFromSelectEnabled =             getBooleanProperty(properties, ConfigurationProperties.IMPLICIT_GROUP_BY_FROM_SELECT,      "true");
        this.implicitGroupByFromHavingEnabled =             getBooleanProperty(properties, ConfigurationProperties.IMPLICIT_GROUP_BY_FROM_HAVING,      "true");
        this.implicitGroupByFromOrderByEnabled =            getBooleanProperty(properties, ConfigurationProperties.IMPLICIT_GROUP_BY_FROM_ORDER_BY,    "true");
        this.valuesClauseFilterNullsEnabled =               getBooleanProperty(properties, ConfigurationProperties.VALUES_CLAUSE_FILTER_NULLS,         "true");
        this.parameterAsLiteralRenderingEnabled =           getBooleanProperty(properties, ConfigurationProperties.PARAMETER_AS_LITERAL_RENDERING,     "true");
        this.optimizedKeysetPredicateRenderingEnabled =     getBooleanProperty(properties, ConfigurationProperties.OPTIMIZED_KEYSET_PREDICATE_RENDERING,     "true");
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
        throw new UnsupportedOperationException("Can't set cacheable on immutable query configuration!");
    }

    @Override
    public boolean isCacheable() {
        return false;
    }

    @Override
    public String getProperty(String name) {
        switch (name) {
            case ConfigurationProperties.COMPATIBLE_MODE: return Boolean.toString(compatibleModeEnabled);
            case ConfigurationProperties.RETURNING_CLAUSE_CASE_SENSITIVE: return Boolean.toString(returningClauseCaseSensitive);
            case ConfigurationProperties.SIZE_TO_COUNT_TRANSFORMATION: return Boolean.toString(sizeToCountTransformationEnabled);
            case ConfigurationProperties.IMPLICIT_GROUP_BY_FROM_SELECT: return Boolean.toString(implicitGroupByFromSelectEnabled);
            case ConfigurationProperties.IMPLICIT_GROUP_BY_FROM_HAVING: return Boolean.toString(implicitGroupByFromHavingEnabled);
            case ConfigurationProperties.IMPLICIT_GROUP_BY_FROM_ORDER_BY: return Boolean.toString(implicitGroupByFromOrderByEnabled);
            case ConfigurationProperties.EXPRESSION_OPTIMIZATION: return Boolean.toString(expressionOptimizationEnabled);
            case ConfigurationProperties.EXPRESSION_CACHE_CLASS: return expressionCacheClass;
            case ConfigurationProperties.VALUES_CLAUSE_FILTER_NULLS: return Boolean.toString(valuesClauseFilterNullsEnabled);
            case ConfigurationProperties.PARAMETER_AS_LITERAL_RENDERING: return Boolean.toString(parameterAsLiteralRenderingEnabled);
            case ConfigurationProperties.OPTIMIZED_KEYSET_PREDICATE_RENDERING: return Boolean.toString(optimizedKeysetPredicateRenderingEnabled);
            default: return null;
        }
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<>(20);
        properties.put(ConfigurationProperties.COMPATIBLE_MODE, Boolean.toString(compatibleModeEnabled));
        properties.put(ConfigurationProperties.RETURNING_CLAUSE_CASE_SENSITIVE, Boolean.toString(returningClauseCaseSensitive));
        properties.put(ConfigurationProperties.SIZE_TO_COUNT_TRANSFORMATION, Boolean.toString(sizeToCountTransformationEnabled));
        properties.put(ConfigurationProperties.IMPLICIT_GROUP_BY_FROM_SELECT, Boolean.toString(implicitGroupByFromSelectEnabled));
        properties.put(ConfigurationProperties.IMPLICIT_GROUP_BY_FROM_HAVING, Boolean.toString(implicitGroupByFromHavingEnabled));
        properties.put(ConfigurationProperties.IMPLICIT_GROUP_BY_FROM_ORDER_BY, Boolean.toString(implicitGroupByFromOrderByEnabled));
        properties.put(ConfigurationProperties.EXPRESSION_OPTIMIZATION, Boolean.toString(expressionOptimizationEnabled));
        properties.put(ConfigurationProperties.EXPRESSION_CACHE_CLASS, expressionCacheClass);
        properties.put(ConfigurationProperties.VALUES_CLAUSE_FILTER_NULLS, Boolean.toString(valuesClauseFilterNullsEnabled));
        properties.put(ConfigurationProperties.PARAMETER_AS_LITERAL_RENDERING, Boolean.toString(parameterAsLiteralRenderingEnabled));
        properties.put(ConfigurationProperties.OPTIMIZED_KEYSET_PREDICATE_RENDERING, Boolean.toString(optimizedKeysetPredicateRenderingEnabled));
        return properties;
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            setProperty(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void setProperty(String propertyName, String propertyValue) {
        throw new IllegalArgumentException("Can't set a property on factory level!");
    }

    private boolean getBooleanProperty(Map<String, String> properties, String propertyName, String defaultValue) {
        return Boolean.parseBoolean(getProperty(properties, propertyName, defaultValue));
    }

    private String getProperty(Map<String, String> properties, String propertyName, String defaultValue) {
        String value = properties.get(propertyName);
        if (value == null) {
            return defaultValue;
        }

        return value;
    }

}
