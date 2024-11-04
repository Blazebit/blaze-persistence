/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.flatview.nested.mutable.model;

import com.blazebit.persistence.testsuite.entity.NameObjectContainer2;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@UpdatableEntityView
@EntityView(NameObjectContainer2.class)
public interface UpdatableNameObjectContainerView2 {

    public String getName();

    @UpdatableMapping
    public UpdatableNameObjectView getNameObject();

    public void setNameObject(UpdatableNameObjectView nameObject);
}
