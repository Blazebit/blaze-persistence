/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.batchsubselect.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionDefault;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionDefaultId;

/**
 *
 * @author Christian Beikov
 * @since 1.6.9
 */
@EntityView(LegacyOrderPositionDefault.class)
public interface LegacyOrderPositionDefaultIdView extends IdHolderView<LegacyOrderPositionDefaultIdView.Id> {

    @EntityView(LegacyOrderPositionDefaultId.class)
    interface Id {

        Long getOrderId();
        void setOrderId(Long orderId);

        Integer getPosition();
        void setPosition(Integer position);

        Integer getSupplierId();
        void setSupplierId(Integer supplierId);
    }
}
