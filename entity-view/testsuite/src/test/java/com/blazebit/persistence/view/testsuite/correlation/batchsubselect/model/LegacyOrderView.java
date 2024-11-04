/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.batchsubselect.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrder;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.6.9
 */
@EntityView(LegacyOrder.class)
public interface LegacyOrderView extends IdHolderView<Long> {

    @Mapping(fetch = FetchStrategy.SUBSELECT)
    Set<LegacyOrderPositionView> getPositions();
}
