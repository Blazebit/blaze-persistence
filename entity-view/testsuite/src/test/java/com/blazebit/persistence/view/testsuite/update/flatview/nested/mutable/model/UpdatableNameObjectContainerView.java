/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.flatview.nested.mutable.model;

import com.blazebit.persistence.testsuite.entity.NameObjectContainer;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@UpdatableEntityView
@EntityView(NameObjectContainer.class)
public interface UpdatableNameObjectContainerView {

    public String getName();

    @UpdatableMapping
    public UpdatableNameObjectView getNameObject();

    public void setNameObject(UpdatableNameObjectView nameObject);
}
