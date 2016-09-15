package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.CriteriaBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public final class EntityViewConfiguration {

    private final CriteriaBuilder<?> criteriaBuilder;
    private final Map<String, Object> optionalParameters;
    private final Map<String, Integer> batchSizeConfiguration;

    public EntityViewConfiguration(CriteriaBuilder<?> criteriaBuilder, Map<String, Object> optionalParameters, Map<String, Object> properties) {
        Map<String, Integer> batchSizeConfiguration = new HashMap<String, Integer>(properties.size());

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
            }
        }

        this.criteriaBuilder = criteriaBuilder;
        this.optionalParameters = new HashMap<String, Object>(optionalParameters);
        this.batchSizeConfiguration = Collections.unmodifiableMap(batchSizeConfiguration);
    }

    public CriteriaBuilder<?> getCriteriaBuilder() {
        return criteriaBuilder;
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
}
