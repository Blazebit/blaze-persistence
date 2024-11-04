/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.entityid.model;

import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.PostCreate;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionDefault;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionDefaultId;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@CreatableEntityView
@UpdatableEntityView
@EntityView(LegacyOrderPositionDefault.class)
public abstract class UpdatableLegacyOrderPositionDefaultView implements LegacyOrderPositionDefaultIdView {

    @PostCreate
    void init() {
        setId(new LegacyOrderPositionDefaultId());
    }
}
