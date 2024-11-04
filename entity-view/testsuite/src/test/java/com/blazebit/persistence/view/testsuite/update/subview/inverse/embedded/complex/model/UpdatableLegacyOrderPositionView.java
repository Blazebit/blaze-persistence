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
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPosition;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@CreatableEntityView
@UpdatableEntityView
@EntityView(LegacyOrderPosition.class)
public interface UpdatableLegacyOrderPositionView extends LegacyOrderPositionIdView {

    String getArticleNumber();
    void setArticleNumber(String articleNumber);

    @UpdatableMapping(cascade = { CascadeType.UPDATE, CascadeType.PERSIST })
    @MappingInverse(removeStrategy = InverseRemoveStrategy.SET_NULL)
    Set<UpdatableLegacyOrderPositionElementView> getElems();
    void setElems(Set<UpdatableLegacyOrderPositionElementView> elements);

    @UpdatableMapping
    @MappingInverse(removeStrategy = InverseRemoveStrategy.REMOVE)
    Set<UpdatableLegacyOrderPositionDefaultView> getDefaults();
    void setDefaults(Set<UpdatableLegacyOrderPositionDefaultView> defaults);
}
