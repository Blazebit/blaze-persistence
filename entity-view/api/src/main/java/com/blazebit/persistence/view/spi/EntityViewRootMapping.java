/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.spi;

import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.view.CorrelationProvider;

import java.util.List;

/**
 * Interface implemented by the entity view provider.
 *
 * Represents the current view that.
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
public interface EntityViewRootMapping {

    /**
     * The name of the entity view root through which it can be accessed in the entity view mappings.
     *
     * @return The entity view root name
     */
    String getName();

    /**
     * The managed type class for which to create this entity view root or <code>null</code> if either {@link #getJoinExpression()} or {@link #getCorrelationProvider()} is defined.
     *
     * @return The entity class
     */
    Class<?> getManagedTypeClass();

    /**
     * The expression to use to create this entity view root or <code>null</code> if either {@link #getManagedTypeClass()} ()} or {@link #getCorrelationProvider()} is defined.
     *
     * @return The expression
     */
    String getJoinExpression();

    /**
     * The class which provides the correlation provider for this entity view root or <code>null</code> if either {@link #getManagedTypeClass()} ()} or {@link #getJoinExpression()} is defined.
     *
     * @return The correlation provider
     */
    public Class<? extends CorrelationProvider> getCorrelationProvider();

    /**
     * The condition expression to use for joining the entity view root.
     *
     * @return The condition expression
     */
    String getConditionExpression();

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
    public List<String> getOrderByItems();

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
