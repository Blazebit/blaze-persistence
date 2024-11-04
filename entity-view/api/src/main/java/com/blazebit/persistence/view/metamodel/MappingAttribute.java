/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;


import com.blazebit.persistence.spi.ServiceProvider;

/**
 * Represents an attribute that has a mapping expression.
 *
 * @param <X> The type of the declaring entity view
 * @param <Y> The type of attribute
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface MappingAttribute<X, Y> extends Attribute<X, Y> {

    /**
     * Returns the mapping of the attribute.
     *
     * @return The mapping of the attribute
     */
    public String getMapping();

    /**
     * Renders the mapping for the given parent expression to the given string builder.
     *
     * @param parent The parent expression
     * @param serviceProvider The service provider
     * @param sb The string builder
     * @since 1.5.0
     */
    public void renderMapping(String parent, ServiceProvider serviceProvider, StringBuilder sb);
}
