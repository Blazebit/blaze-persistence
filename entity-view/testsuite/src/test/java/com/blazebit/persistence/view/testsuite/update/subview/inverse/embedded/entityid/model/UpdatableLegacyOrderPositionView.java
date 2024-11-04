/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.entityid.model;

import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.InverseRemoveStrategy;
import com.blazebit.persistence.view.MappingInverse;
import com.blazebit.persistence.view.PostCreate;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPosition;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionId;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@CreatableEntityView
@UpdatableEntityView
@EntityView(LegacyOrderPosition.class)
public abstract class UpdatableLegacyOrderPositionView implements LegacyOrderPositionIdView {

    @PostCreate
    void init() {
        setId(new LegacyOrderPositionId());
    }

    public abstract String getArticleNumber();
    public abstract void setArticleNumber(String articleNumber);

    @UpdatableMapping
    @MappingInverse(removeStrategy = InverseRemoveStrategy.REMOVE)
    public abstract Set<LegacyOrderPositionDefaultIdView> getDefaults();
    abstract void setDefaults(Set<LegacyOrderPositionDefaultIdView> defaults);
}
