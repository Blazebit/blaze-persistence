/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.batchsubselect.model;

import com.blazebit.persistence.view.BatchFetch;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPosition;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionElement;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionId;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.6.9
 */
@EntityView(LegacyOrderPosition.class)
public interface LegacyOrderPositionView extends IdHolderView<LegacyOrderPositionView.Id> {

    String getArticleNumber();

    @BatchFetch(size = 10)
    @Mapping(fetch = FetchStrategy.SELECT)
    Set<LegacyOrderPositionElementView> getElems();

    @EntityView(LegacyOrderPositionId.class)
    interface Id {

        Long getOrderId();
        void setOrderId(Long orderId);

        Integer getPositionId();
        void setPositionId(Integer positionId);
    }
}
