/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.flatview.simple.mutable.model;

import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.view.CascadeType;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@UpdatableEntityView
@EntityView(NameObject.class)
public interface UpdatableNameObjectView {

    public String getPrimaryName();

    public void setPrimaryName(String primaryName);

    @UpdatableMapping(cascade = { CascadeType.PERSIST })
    public IntIdEntityIdView getIntIdEntity();

    public void setIntIdEntity(IntIdEntityIdView intIdEntity);
}
