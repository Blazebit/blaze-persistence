/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.proxy.model;

import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@UpdatableEntityView
@EntityView(NameObject.class)
public interface UpdatableNameObjectView extends NameObjectView{

    public void setPrimaryName(String primaryName);
}
