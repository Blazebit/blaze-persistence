/*
 * Copyright 2014 - 2023 Blazebit.
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
import com.blazebit.persistence.view.ConfigurationProperties;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.spi.ViewJpqlMacro;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public final class EntityViewConfiguration {

    private static final NavigableSet<String> EMPTY_SET = new TreeSet<>();
    private final FullQueryBuilder<?, ?> criteriaBuilder;
    private final ExpressionFactory expressionFactory;
    private final ViewJpqlMacro viewJpqlMacro;
    private final EmbeddingViewJpqlMacro embeddingViewJpqlMacro;
    private final Map<String, Object> optionalParameters;
    private final NavigableSet<String> fetches;
    private final Map<String, Integer> batchSizeConfiguration;
    private final Map<String, BatchCorrelationMode> expectBatchCorrelationValuesConfiguration;

    public EntityViewConfiguration(FullQueryBuilder<?, ?> criteriaBuilder, ExpressionFactory expressionFactory, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro, Map<String, Object> optionalParameters, Map<String, Object> properties, Collection<String> fetches, String attributePath) {
        this(criteriaBuilder, expressionFactory, viewJpqlMacro, embeddingViewJpqlMacro, optionalParameters, properties, getFetches(fetches, attributePath));
    }

    public EntityViewConfiguration(FullQueryBuilder<?, ?> criteriaBuilder, ExpressionFactory expressionFactory, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro, Map<String, Object> optionalParameters, Map<String, Object> properties, Collection<String> fetches, ManagedViewTypeImplementor<?> managedViewType) {
        this(criteriaBuilder, expressionFactory, viewJpqlMacro, embeddingViewJpqlMacro, optionalParameters, properties, getFetches(fetches, managedViewType));
    }

    private EntityViewConfiguration(FullQueryBuilder<?, ?> criteriaBuilder, ExpressionFactory expressionFactory, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro, Map<String, Object> optionalParameters, Map<String, Object> properties, NavigableSet<String> fetches) {
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
        this.viewJpqlMacro = viewJpqlMacro;
        this.embeddingViewJpqlMacro = embeddingViewJpqlMacro;
        this.optionalParameters = new HashMap<>(optionalParameters);
        this.fetches = fetches;
        this.batchSizeConfiguration = Collections.unmodifiableMap(batchSizeConfiguration);
        this.expectBatchCorrelationValuesConfiguration = Collections.unmodifiableMap(expectBatchCorrelationValuesConfiguration);
        this.criteriaBuilder.registerMacro("view", viewJpqlMacro);
        this.criteriaBuilder.registerMacro("embedding_view", embeddingViewJpqlMacro);
    }

    private EntityViewConfiguration(EntityViewConfiguration original, FullQueryBuilder<?, ?> criteriaBuilder, NavigableSet<String> fetches, EmbeddingViewJpqlMacro embeddingViewJpqlMacro) {
        this.criteriaBuilder = criteriaBuilder;
        this.expressionFactory = original.expressionFactory;
        this.viewJpqlMacro = original.viewJpqlMacro;
        this.embeddingViewJpqlMacro = embeddingViewJpqlMacro;
        this.optionalParameters = original.optionalParameters;
        this.fetches = fetches;
        this.batchSizeConfiguration = original.batchSizeConfiguration;
        this.expectBatchCorrelationValuesConfiguration = original.expectBatchCorrelationValuesConfiguration;
    }

    private static NavigableSet<String> getFetches(Collection<String> fetches, String attributePath) {
        NavigableSet<String> filteredFetches;
        if (fetches.isEmpty()) {
            filteredFetches = EMPTY_SET;
        } else {
            filteredFetches = new TreeSet<>();
            String prefix = attributePath + ".";
            for (String fetch : fetches) {
                if (fetch.startsWith(prefix)) {
                    filteredFetches.add(fetch.substring(prefix.length()));
                }
            }
        }

        return filteredFetches;
    }

    private static NavigableSet<String> getFetches(Collection<String> fetches, ManagedViewTypeImplementor<?> managedViewType) {
        NavigableSet<String> filteredFetches;
        if (fetches.isEmpty()) {
            filteredFetches = EMPTY_SET;
        } else {
            filteredFetches = new TreeSet<>();
            if (managedViewType instanceof ViewType<?>) {
                addIdFetches((ViewType<?>) managedViewType, filteredFetches, new StringBuilder());
            }
            for (String fetch : fetches) {
                String[] parts = fetch.split("\\.");
                Type<?> t = managedViewType;
                StringBuilder sb = new StringBuilder();
                int i = 0;
                // NOTE: We could actually also support dynamic entity fetches here
                while (t instanceof ManagedViewTypeImplementor<?>) {
                    ManagedViewTypeImplementor<?> viewType = (ManagedViewTypeImplementor<?>) t;
                    if (i != 0 && viewType instanceof ViewType<?>) {
                        addIdFetches((ViewType<?>) viewType, filteredFetches, sb);
                    }
                    if (i == parts.length) {
                        // Fetch the whole subtree
                        int length = sb.length();
                        for (String path : viewType.getRecursiveAttributes().keySet()) {
                            sb.setLength(length);
                            sb.append(path);
                            filteredFetches.add(sb.toString());
                        }
                        break;
                    } else {
                        // Fetch a specific attribute
                        sb.append(parts[i]);
                        MethodAttribute<?, ?> attribute = viewType.getAttribute(parts[i]);
                        if (attribute == null) {
                            // fallback for viewType with @EntityViewInheritance
                            attribute = viewType.getRecursiveAttributes().get(parts[i]);
                        }
                        if (attribute instanceof PluralAttribute<?, ?, ?>) {
                            t = ((PluralAttribute<?, ?, ?>) attribute).getElementType();
                        } else {
                            t = ((SingularAttribute<?, ?>) attribute).getType();
                        }
                        // For select and subselect correlation fetched attributes we must add the correlated attribute path also to fetch the correlation basis expression
                        if ((attribute.getFetchStrategy() == FetchStrategy.SELECT || attribute.getFetchStrategy() == FetchStrategy.SUBSELECT)
                                && ((AbstractMethodAttribute<?, ?>) attribute).getCorrelationProviderFactory() != null) {
                            filteredFetches.add(sb.toString());
                        }
                        sb.append('.');
                        i++;
                    }
                }
                filteredFetches.add(fetch);
            }
        }

        return filteredFetches;
    }

    private static void addIdFetches(ViewType<?> viewType, Set<String> filteredFetches, StringBuilder sb) {
        MethodAttribute<?, ?> idAttribute = viewType.getIdAttribute();
        String idName = idAttribute.getName();
        StringBuilder idSb = new StringBuilder(sb.length() + idName.length()).append(sb).append(idName);

        filteredFetches.add(idSb.toString());

        Type<?> idType = ((SingularAttribute<?, ?>) idAttribute).getType();
        if (idType instanceof ManagedViewTypeImplementor<?>) {
            idSb.append('.');
            int length = idSb.length();
            for (String path : ((ManagedViewTypeImplementor<?>) idType).getRecursiveAttributes().keySet()) {
                idSb.setLength(length);
                idSb.append(path);
                filteredFetches.add(idSb.toString());
            }
        }
    }

    public EntityViewConfiguration forSubview(FullQueryBuilder<?, ?> criteriaBuilder, String attributePath, EmbeddingViewJpqlMacro embeddingViewJpqlMacro) {
        return new EntityViewConfiguration(this, criteriaBuilder, getFetches(fetches, attributePath), embeddingViewJpqlMacro);
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

    public NavigableSet<String> getFetches() {
        return fetches;
    }

    public boolean hasSubFetches(String attributePath) {
        if (fetches.isEmpty()) {
            return true;
        }
        String fetchedPath = fetches.ceiling(attributePath);
        return fetchedPath != null && (fetchedPath.length() == attributePath.length() || fetchedPath.startsWith(attributePath) && fetchedPath.length() > attributePath.length() && fetchedPath.charAt(attributePath.length()) == '.');
    }

    public ViewJpqlMacro getViewJpqlMacro() {
        return viewJpqlMacro;
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
