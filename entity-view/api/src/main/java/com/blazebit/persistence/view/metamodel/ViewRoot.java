/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.CorrelationProviderFactory;

import javax.persistence.metamodel.Type;
import java.util.List;

/**
 * An entity view root.
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
public interface ViewRoot {

    /**
     * The name of the entity view root through which it can be accessed in the entity view mappings.
     *
     * @return The entity view root name
     */
    String getName();

    /**
     * Returns the type of the view root. May be <code>null</code> if not resolvable.
     *
     * @return The type of the view root
     */
    public Type<?> getType();

    /**
     * Returns the correlation provider factory of the view root.
     *
     * @return The correlation provider factory of the view root
     */
    public CorrelationProviderFactory getCorrelationProviderFactory();

    /**
     * Returns the correlation provider of the view root.
     *
     * @return The correlation provider of the view root
     */
    public Class<? extends CorrelationProvider> getCorrelationProvider();

    /**
     * The join type to use for the entity view root.
     *
     * @return The join type
     */
    JoinType getJoinType();

    /**
     * The associations that should be fetched along with the entity mapped by this attribute.
     *
     * @return The association that should be fetched
     */
    public String[] getFetches();

    /**
     * Returns the order by items for the limit expression.
     *
     * @return The order by items for the limit expression
     */
    public List<OrderByItem> getOrderByItems();

    /**
     * Returns the limit expression.
     *
     * @return The limit expression
     */
    public String getLimitExpression();

    /**
     * Returns the offset expression.
     *
     * @return The offset expression
     */
    public String getOffsetExpression();
}
