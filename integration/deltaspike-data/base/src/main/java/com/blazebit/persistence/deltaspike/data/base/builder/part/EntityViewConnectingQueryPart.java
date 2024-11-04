/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.base.builder.part;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.part.ConnectingQueryPart} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public abstract class EntityViewConnectingQueryPart extends EntityViewQueryPart {
    protected final boolean first;

    public EntityViewConnectingQueryPart(boolean first) {
        this.first = first;
    }

    public boolean isFirst() {
        return first;
    }
}