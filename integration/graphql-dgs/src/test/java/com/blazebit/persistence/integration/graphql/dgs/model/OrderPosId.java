/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql.dgs.model;

import java.io.Serializable;
import javax.persistence.Embeddable;
import javax.persistence.Id;

/**
 * @author Christian Beikov
 * @since 1.6.16
 */
@Embeddable
public class OrderPosId implements Serializable {

    private Long orderId;
    private Long position;

    public OrderPosId() {
    }

    public OrderPosId(Long orderId, Long position) {
        this.orderId = orderId;
        this.position = position;
    }
}
