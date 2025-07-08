/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql.dgs.view;

import com.blazebit.persistence.integration.graphql.dgs.model.OrderPos;
import com.blazebit.persistence.integration.graphql.dgs.model.OrderPosId;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

/**
 * @author Christian Beikov
 * @since 1.6.16
 */
@EntityView(OrderPos.class)
public interface OrderPosIdView {

    @IdMapping
    Id getId();

    @EntityView(OrderPosId.class)
    interface Id {
        Long getOrderId();
        Long getPosition();
    }

}
