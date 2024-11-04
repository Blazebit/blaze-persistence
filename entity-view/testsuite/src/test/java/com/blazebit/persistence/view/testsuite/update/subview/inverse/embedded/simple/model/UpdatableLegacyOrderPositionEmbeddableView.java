/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.simple.model;

import java.io.Serializable;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPosition;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionEmbeddable;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionId;

/**
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
@UpdatableEntityView
@EntityView(LegacyOrderPositionEmbeddable.class)
public interface UpdatableLegacyOrderPositionEmbeddableView extends Serializable {

    String getName();
    void setName(String name);
}
