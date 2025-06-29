/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.field.model;

import com.blazebit.persistence.testsuite.entity.Root2;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.testsuite.basic.model.IdHolderView;

/**
 *
 * @author Christian Beikov
 * @since 1.6.16
 */
@EntityView(Root2.class)
public interface Root2View extends IdHolderView<Integer> {

    public String getName();
    public void setName(String name);
}
