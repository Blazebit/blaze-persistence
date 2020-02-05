/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation;

import com.blazebit.persistence.CorrelationQueryBuilder;
import com.blazebit.persistence.FromProvider;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.spi.ServiceProvider;
import com.blazebit.persistence.view.CorrelationBuilder;

import javax.persistence.metamodel.EntityType;

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
}
