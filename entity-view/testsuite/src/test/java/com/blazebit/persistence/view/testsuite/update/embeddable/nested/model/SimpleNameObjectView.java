/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.embeddable.nested.model;

import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@UpdatableEntityView
@EntityView(NameObject.class)
public interface SimpleNameObjectView {

    public String getPrimaryName();

    public void setPrimaryName(String primaryName);

    public String getSecondaryName();

    public void setSecondaryName(String secondaryName);

    public SimpleIntIdEntityView getIntIdEntity();

    public void setIntIdEntity(SimpleIntIdEntityView intIdEntity);

}
