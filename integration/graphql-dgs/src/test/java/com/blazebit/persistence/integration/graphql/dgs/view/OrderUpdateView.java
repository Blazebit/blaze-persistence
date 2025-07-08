/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql.dgs.view;

import com.blazebit.persistence.integration.graphql.dgs.model.Order;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 * @author Christian Beikov
 * @since 1.6.16
 */
@UpdatableEntityView
@EntityView(Order.class)
public interface OrderUpdateView {

    @IdMapping
    Long getId();
    void setId(Long id);

    OrderPosIdView getAdditionalPosition();
    void setAdditionalPosition(OrderPosIdView additionalPosition);

    String getName();
    void setName(String name);
}
