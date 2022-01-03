/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.view.metamodel;

import com.blazebit.persistence.spi.ServiceProvider;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.CorrelationProviderFactory;

/**
 * Instances of the type {@linkplain CorrelatedAttribute} represents single-valued properties or fields.
 *
 * @param <X> The type of the declaring entity view
 * @param <Y> The type of attribute
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface CorrelatedAttribute<X, Y> extends Attribute<X, Y> {

    /**
     * Returns the correlation provider factory of the attribute.
     *
     * @return The correlation provider factory of the attribute
     * @since 1.4.0
     */
    public CorrelationProviderFactory getCorrelationProviderFactory();

    /**
     * Returns the correlation provider of the attribute.
     *
     * @return The correlation provider of the attribute
     */
    public Class<? extends CorrelationProvider> getCorrelationProvider();

    /**
     * Returns the correlation basis of the attribute.
     *
     * @return The correlation basis of the attribute
     */
    public String getCorrelationBasis();

    /**
     * Returns the correlation result of the attribute.
     *
     * @return The correlation result of the attribute
     */
    public String getCorrelationResult();

    /**
     * Renders the correlation basis expression for the given parent expression to the given string builder.
     *
     * @param parent The parent expression
     * @param serviceProvider The service provider
     * @param sb The string builder
     * @since 1.5.0
     */
    public void renderCorrelationBasis(String parent, ServiceProvider serviceProvider, StringBuilder sb);

    /**
     * Renders the correlation result expression for the given parent expression to the given string builder.
     *
     * @param parent The parent expression
     * @param serviceProvider The service provider
     * @param sb The string builder
     * @since 1.5.0
     */
    public void renderCorrelationResult(String parent, ServiceProvider serviceProvider, StringBuilder sb);

}
