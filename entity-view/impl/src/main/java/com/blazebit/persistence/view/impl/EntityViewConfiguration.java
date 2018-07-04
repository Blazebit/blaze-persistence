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

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.view.impl.macro.EmbeddingViewJpqlMacro;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public final class EntityViewConfiguration {

    private final FullQueryBuilder<?, ?> criteriaBuilder;
    private final ExpressionFactory expressionFactory;
    private final EmbeddingViewJpqlMacro embeddingViewJpqlMacro;
    private final Map<String, Object> optionalParameters;
    private final Map<String, Integer> batchSizeConfiguration;
    private final Map<String, BatchCorrelationMode> expectBatchCorrelationValuesConfiguration;

    public EntityViewConfiguration(FullQueryBuilder<?, ?> criteriaBuilder, ExpressionFactory expressionFactory, EmbeddingViewJpqlMacro embeddingViewJpqlMacro, Map<String, Object> optionalParameters, Map<String, Object> properties) {
        Map<String, Integer> batchSizeConfiguration = new HashMap<String, Integer>(properties.size());
        Map<String, BatchCorrelationMode> expectBatchCorrelationValuesConfiguration = new HashMap<>(properties.size());

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
                BatchCorrelationMode value = getExpectBatchCorrelationValues(key, entry.getValue());
                if (key.length() == ConfigurationProperties.EXPECT_BATCH_CORRELATION_VALUES.length()) {
                    expectBatchCorrelationValuesConfiguration.put("", value);
                } else if (key.length() < ConfigurationProperties.EXPECT_BATCH_CORRELATION_VALUES.length() + 2) {
                    throw new IllegalArgumentException("Invalid batch correlation values expectation configuration!");
                } else {
                    key = key.substring(ConfigurationProperties.EXPECT_BATCH_CORRELATION_VALUES.length() + 1);
                    expectBatchCorrelationValuesConfiguration.put(key, value);
                }
            } else if (key.startsWith(ConfigurationProperties.EXPECT_BATCH_MODE)) {
                BatchCorrelationMode value = getExpectBatchCorrelationValues(key, entry.getValue());
                if (key.length() == ConfigurationProperties.EXPECT_BATCH_MODE.length()) {
                    expectBatchCorrelationValuesConfiguration.put("", value);
                } else if (key.length() < ConfigurationProperties.EXPECT_BATCH_MODE.length() + 2) {
                    throw new IllegalArgumentException("Invalid batch mode expectation configuration!");
                } else {
                    key = key.substring(ConfigurationProperties.EXPECT_BATCH_MODE.length() + 1);
                    expectBatchCorrelationValuesConfiguration.put(key, value);
                }
            }
        }

        this.criteriaBuilder = criteriaBuilder;
        this.expressionFactory = expressionFactory;
        this.embeddingViewJpqlMacro = embeddingViewJpqlMacro;
        this.optionalParameters = new HashMap<String, Object>(optionalParameters);
        this.batchSizeConfiguration = Collections.unmodifiableMap(batchSizeConfiguration);
        this.expectBatchCorrelationValuesConfiguration = Collections.unmodifiableMap(expectBatchCorrelationValuesConfiguration);
        this.criteriaBuilder.registerMacro("embedding_view", embeddingViewJpqlMacro);
    }

    private EntityViewConfiguration(EntityViewConfiguration original, FullQueryBuilder<?, ?> criteriaBuilder, String attributePath, EmbeddingViewJpqlMacro embeddingViewJpqlMacro) {
        this.criteriaBuilder = criteriaBuilder;
        this.expressionFactory = original.expressionFactory;
        this.embeddingViewJpqlMacro = embeddingViewJpqlMacro;
        this.optionalParameters = original.optionalParameters;
        this.batchSizeConfiguration = original.batchSizeConfiguration;
        this.expectBatchCorrelationValuesConfiguration = original.expectBatchCorrelationValuesConfiguration;
    }

    public EntityViewConfiguration forSubview(FullQueryBuilder<?, ?> criteriaBuilder, String attributePath, EmbeddingViewJpqlMacro embeddingViewJpqlMacro) {
        return new EntityViewConfiguration(this, criteriaBuilder, attributePath, embeddingViewJpqlMacro);
    }

    public FullQueryBuilder<?, ?> getCriteriaBuilder() {
        return criteriaBuilder;
    }

    public ExpressionFactory getExpressionFactory() {
        return expressionFactory;
    }

    public Map<String, Object> getOptionalParameters() {
        return optionalParameters;
    }

    public EmbeddingViewJpqlMacro getEmbeddingViewJpqlMacro() {
        return embeddingViewJpqlMacro;
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

    public BatchCorrelationMode getExpectBatchCorrelationValues() {
        return getExpectBatchCorrelationValues("");
    }

    public BatchCorrelationMode getExpectBatchCorrelationValues(String attributePath) {
        // By default we expect to batch correlation values because the usage of the view root is less probable
        return getExpectBatchCorrelationValues(attributePath, BatchCorrelationMode.VALUES);
    }

    private BatchCorrelationMode getExpectBatchCorrelationValues(String attributePath, BatchCorrelationMode defaultValue) {
        BatchCorrelationMode value = expectBatchCorrelationValuesConfiguration.get(attributePath);
        if (value == null) {
            value = expectBatchCorrelationValuesConfiguration.get("");
        }
        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    private BatchCorrelationMode getExpectBatchCorrelationValues(String key, Object value) {
        BatchCorrelationMode val = null;
        if (value instanceof Boolean) {
            val = (Boolean) value ? BatchCorrelationMode.VALUES : BatchCorrelationMode.VIEW_ROOTS;
        } else if (value instanceof String) {
            String s = (String) value;
            if ("true".equalsIgnoreCase(s) || "values".equalsIgnoreCase(s)) {
                val = BatchCorrelationMode.VALUES;
            } else if ("false".equalsIgnoreCase(s) || "view_roots".equalsIgnoreCase(s)) {
                val = BatchCorrelationMode.VIEW_ROOTS;
            } else if ("embedding_views".equalsIgnoreCase(s)) {
                val = BatchCorrelationMode.EMBEDDING_VIEWS;
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
