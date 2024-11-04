/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.complex.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPosition;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionId;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(LegacyOrderPosition.class)
public interface LegacyOrderPositionIdView extends IdHolderView<LegacyOrderPositionIdView.Id> {

    @EntityView(LegacyOrderPositionId.class)
    interface Id {

        Long getOrderId();
        void setOrderId(Long orderId);

        Integer getPositionId();
        void setPositionId(Integer positionId);
    }
}
