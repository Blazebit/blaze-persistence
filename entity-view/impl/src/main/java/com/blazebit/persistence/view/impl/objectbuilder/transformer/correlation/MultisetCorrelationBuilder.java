/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation;

import com.blazebit.persistence.CorrelationQueryBuilder;
import com.blazebit.persistence.FromProvider;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.spi.ServiceProvider;
import com.blazebit.persistence.view.CorrelationBuilder;

import jakarta.persistence.metamodel.EntityType;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MultisetCorrelationBuilder implements CorrelationBuilder {

    private final SubqueryInitiator<?> subqueryInitiator;
    private final ServiceProvider serviceProvider;
    private final String correlationAlias;
    private SubqueryBuilder<?> subqueryBuilder;

    public MultisetCorrelationBuilder(SubqueryInitiator<?> subqueryInitiator, ServiceProvider serviceProvider, String correlationAlias) {
        this.subqueryInitiator = subqueryInitiator;
        this.serviceProvider = serviceProvider;
        this.correlationAlias = correlationAlias;
    }

    @Override
    public <T> T getService(Class<T> serviceClass) {
        return serviceProvider.getService(serviceClass);
    }

    @Override
    public FromProvider getCorrelationFromProvider() {
        return subqueryBuilder;
    }

    @Override
    public String getCorrelationAlias() {
        return correlationAlias;
    }

    public SubqueryBuilder<?> getSubqueryBuilder() {
        return subqueryBuilder;
    }

    @Override
    public JoinOnBuilder<CorrelationQueryBuilder> correlate(Class<?> entityClass) {
        if (subqueryBuilder != null) {
            throw new IllegalArgumentException("Can not correlate with multiple entity classes!");
        }

        subqueryBuilder = subqueryInitiator.from(entityClass, correlationAlias);
        return subqueryBuilder.getService(JoinOnBuilder.class);
    }

    @Override
    public JoinOnBuilder<CorrelationQueryBuilder> correlate(EntityType<?> entityType) {
        if (subqueryBuilder != null) {
            throw new IllegalArgumentException("Can not correlate with multiple entity classes!");
        }

        subqueryBuilder = subqueryInitiator.from(entityType, correlationAlias);
        return subqueryBuilder.getService(JoinOnBuilder.class);
    }

    @Override
    public JoinOnBuilder<CorrelationQueryBuilder> correlate(String correlationPath) {
        if (subqueryBuilder != null) {
            throw new IllegalArgumentException("Can not correlate with multiple entity classes!");
        }

        subqueryBuilder = subqueryInitiator.from(correlationPath, correlationAlias);
        return subqueryBuilder.getService(JoinOnBuilder.class);
    }
}
