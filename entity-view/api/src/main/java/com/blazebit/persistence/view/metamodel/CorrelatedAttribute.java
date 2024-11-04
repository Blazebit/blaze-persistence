/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
