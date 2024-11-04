/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import com.blazebit.persistence.CorrelationQueryBuilder;
import com.blazebit.persistence.FromProvider;
import com.blazebit.persistence.JoinOnBuilder;

import javax.persistence.metamodel.EntityType;

/**
 * A builder for correlating a basis with an entity class.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface CorrelationBuilder {

    /**
     * Returns the service or null if none is available.
     *
     * @param serviceClass The type of the service
     * @param <T> The service type
     * @return The service or null
     */
    public <T> T getService(Class<T> serviceClass);

    /**
     * Returns the correlation from provider.
     *
     * @return The correlation from provider
     * @since 1.3.0
     */
    public FromProvider getCorrelationFromProvider();

    /**
     * Generates a meaningful alias that can be used for the correlation.
     *
     * @return The generated alias
     */
    public String getCorrelationAlias();

    /**
     * Correlates a basis with the given entity class.
     *
     * @param entityClass The entity class which should be correlated
     * @return The restriction builder for the correlation predicate
     */
    public JoinOnBuilder<CorrelationQueryBuilder> correlate(Class<?> entityClass);

    /**
     * Correlates a basis with the given entity type.
     *
     * @param entityType The entity type which should be correlated
     * @return The restriction builder for the correlation predicate
     * @since 1.3.0
     */
    public JoinOnBuilder<CorrelationQueryBuilder> correlate(EntityType<?> entityType);

    /**
     * Correlates a path expression.
     *
     * @param correlationPath The path to correlate
     * @return The restriction builder for the correlation predicate
     * @since 1.5.0
     */
    public JoinOnBuilder<CorrelationQueryBuilder> correlate(String correlationPath);
}
