/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.flatview.simple.mutable.model;

import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;

/**
 *
 * @author Christian Beikov
 * @since 1.6.4
 */
@CreatableEntityView
@EntityView(IntIdEntity.class)
public interface IntIdEntityCreateView extends IntIdEntityIdView {

    public String getName();

    public void setName(String name);

}
