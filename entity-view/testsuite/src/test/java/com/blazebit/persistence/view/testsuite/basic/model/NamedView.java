/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.testsuite.TypeUseAnnotation;
import com.blazebit.persistence.view.testsuite.entity.NamedEntity;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(NamedEntity.class)
public interface NamedView extends IdHolderView<Integer> {

    @TypeUseAnnotation
    public String getName();
}
