/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

import com.blazebit.persistence.spi.ServiceProvider;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.view.SubqueryProviderFactory;

/**
 * Instances of the type {@linkplain SubqueryAttribute} represents single-valued properties or fields.
 *
 * @param <X> The type of the declaring entity view
 * @param <Y> The type of attribute
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface SubqueryAttribute<X, Y> extends SingularAttribute<X, Y> {

    /**
     * Returns the subquery provider factory of the attribute.
     *
     * @return The subquery provider factory of the attribute
     * @since 1.4.0
     */
    public SubqueryProviderFactory getSubqueryProviderFactory();

    /**
     * Returns the subquery provider of the attribute.
     *
     * @return The subquery provider of the attribute
     */
    public Class<? extends SubqueryProvider> getSubqueryProvider();

    /**
     * Returns the subquery expression of the attribute.
     *
     * @return The subquery expression of the attribute
     */
    public String getSubqueryExpression();

    /**
     * Returns the subquery alias of the attribute.
     *
     * @return The subquery alias of the attribute
     */
    public String getSubqueryAlias();

    /**
     * Renders the subquery expression for the given parent expression to the given string builder.
     *
     * @param parent The parent expression
     * @param serviceProvider The service provider
     * @param sb The string builder
     * @since 1.5.0
     */
    public void renderSubqueryExpression(String parent, ServiceProvider serviceProvider, StringBuilder sb);

    /**
     * Renders the given subquery expression for the given parent expression to the given string builder.
     *
     * @param parent The parent expression
     * @param subqueryExpression The subquery expression
     * @param subqueryAlias The subquery alias
     * @param serviceProvider The service provider
     * @param sb The string builder
     * @since 1.5.0
     */
    public void renderSubqueryExpression(String parent, String subqueryExpression, String subqueryAlias, ServiceProvider serviceProvider, StringBuilder sb);
}
