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

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.impl.expression.ExpressionFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public final class EntityViewConfiguration {

    private final CriteriaBuilder<?> criteriaBuilder;
    private final ExpressionFactory expressionFactory;
    private final Map<String, Object> optionalParameters;
    private final Map<String, Integer> batchSizeConfiguration;
    private final Map<String, Boolean> expectBatchCorrelationValuesConfiguration;

    public EntityViewConfiguration(CriteriaBuilder<?> criteriaBuilder, ExpressionFactory expressionFactory, Map<String, Object> optionalParameters, Map<String, Object> properties) {
        Map<String, Integer> batchSizeConfiguration = new HashMap<String, Integer>(properties.size());
        Map<String, Boolean> expectBatchCorrelationValuesConfiguration = new HashMap<>(properties.size());

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(ConfigurationProperties.DEFAULT_BATCH_SIZE)) {
                Integer value = getBatchSize(key, entry.getValue());
                if (key.length() == ConfigurationProperties.DEFAULT_BATCH_SIZE.length()) {
                    batchSizeConfiguration.put("", value);
                } else if (key.length() < ConfigurationProperties.DEFAULT_BATCH_SIZE.length() + 2) {
                    throw new IllegalArgumentException("Invalid batch size configuration!");
                } else {
                    key = key.substring(ConfigurationProperties.DEFAULT_BATCH_SIZE.length() + 1);
                    batchSizeConfiguration.put(key, value);
                }
            } else if (key.startsWith(ConfigurationProperties.EXPECT_BATCH_CORRELATION_VALUES)) {
                Boolean value = getExpectBatchCorrelationValues(key, entry.getValue());
                if (key.length() == ConfigurationProperties.EXPECT_BATCH_CORRELATION_VALUES.length()) {
                    expectBatchCorrelationValuesConfiguration.put("", value);
                } else if (key.length() < ConfigurationProperties.EXPECT_BATCH_CORRELATION_VALUES.length() + 2) {
                    throw new IllegalArgumentException("Invalid batch correlation values expectation configuration!");
                } else {
                    key = key.substring(ConfigurationProperties.EXPECT_BATCH_CORRELATION_VALUES.length() + 1);
                    expectBatchCorrelationValuesConfiguration.put(key, value);
                }

            }
        }

        this.criteriaBuilder = criteriaBuilder;
        this.expressionFactory = expressionFactory;
        this.optionalParameters = new HashMap<String, Object>(optionalParameters);
        this.batchSizeConfiguration = Collections.unmodifiableMap(batchSizeConfiguration);
        this.expectBatchCorrelationValuesConfiguration = Collections.unmodifiableMap(expectBatchCorrelationValuesConfiguration);
    }

    public CriteriaBuilder<?> getCriteriaBuilder() {
        return criteriaBuilder;
    }

    public ExpressionFactory getExpressionFactory() {
        return expressionFactory;
    }

    public Map<String, Object> getOptionalParameters() {
        return optionalParameters;
    }

    public int getBatchSize() {
        return getBatchSize("");
    }

    public int getBatchSize(String attributePath) {
        return getBatchSize(attributePath, -1);
    }

    public int getBatchSize(String attributePath, int defaultValue) {
        Integer value = batchSizeConfiguration.get(attributePath);
        if (value == null) {
            value = batchSizeConfiguration.get("");
        }
        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    private Integer getBatchSize(String key, Object value) {
        Integer val = null;
        if (value instanceof Integer) {
            val = (Integer) value;
        } else if (value instanceof String) {
            val = Integer.parseInt(value.toString());
        }

        if (val == null) {
            throw new IllegalArgumentException("Invalid batch size configuration for key: " + key);
        } else if (val < 1) {
            throw new IllegalArgumentException("Invalid batch size configuration " + val + " for key: " + key);
        }

        return val;
    }

    public boolean getExpectBatchCorrelationValues() {
        return getExpectBatchCorrelationValues("");
    }

    public boolean getExpectBatchCorrelationValues(String attributePath) {
        // By default we expect to batch correlation values because the usage of the view root is less probable
        return getExpectBatchCorrelationValues(attributePath, true);
    }

    public boolean getExpectBatchCorrelationValues(String attributePath, boolean defaultValue) {
        Boolean value = expectBatchCorrelationValuesConfiguration.get(attributePath);
        if (value == null) {
            value = expectBatchCorrelationValuesConfiguration.get("");
        }
        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    private Boolean getExpectBatchCorrelationValues(String key, Object value) {
        Boolean val = null;
        if (value instanceof Boolean) {
            val = (Boolean) value;
        } else if (value instanceof String) {
            String s = (String) value;
            if ("true".equalsIgnoreCase(s)) {
                val = true;
            } else if ("false".equalsIgnoreCase(s)) {
                val = false;
            } else {
                throw new IllegalArgumentException("Invalid batch correlation expectation configuration " + s + " for key: " + key);
            }
        }

        if (val == null) {
            throw new IllegalArgumentException("Invalid batch correlation expectation configuration for key: " + key);
        }

        return val;
    }
}
