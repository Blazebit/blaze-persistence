/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.complex.model;

import com.blazebit.persistence.view.CascadeType;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.InverseRemoveStrategy;
import com.blazebit.persistence.view.MappingInverse;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrder;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@CreatableEntityView
@UpdatableEntityView
@EntityView(LegacyOrder.class)
public interface UpdatableLegacyOrderView extends LegacyOrderIdView {

    @MappingInverse(removeStrategy = InverseRemoveStrategy.REMOVE)
    @UpdatableMapping(cascade = { CascadeType.UPDATE, CascadeType.PERSIST })
    Set<UpdatableLegacyOrderPositionView> getPositions();
    void setPositions(Set<UpdatableLegacyOrderPositionView> positions);
}
