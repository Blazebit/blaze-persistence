/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.simple.model;

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

    UpdatableLegacyOrderPositionEmbeddableView getEmbeddable();
    void setEmbeddable(UpdatableLegacyOrderPositionEmbeddableView embeddable);

    @UpdatableMapping
    @MappingInverse(removeStrategy = InverseRemoveStrategy.REMOVE)
    Set<LegacyOrderPositionDefaultIdView> getDefaults();
    void setDefaults(Set<LegacyOrderPositionDefaultIdView> defaults);
}
