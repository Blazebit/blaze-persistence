/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.complex.model;

import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPosition;

import java.util.Calendar;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@CreatableEntityView
@UpdatableEntityView
@EntityView(LegacyOrderPosition.class)
public interface CreatableLegacyOrderPositionView extends UpdatableLegacyOrderPositionView {

    Calendar getCreationDate();
    void setCreationDate(Calendar creationDate);
}
