/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.complex.model;

import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionElement;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@CreatableEntityView
@EntityView(LegacyOrderPositionElement.class)
public interface CreatableLegacyOrderPositionElementView extends UpdatableLegacyOrderPositionElementView {

    Long getOrderId();
    void setOrderId(Long orderId);

    Integer getPosition();
    void setPosition(Integer position);
}
