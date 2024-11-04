/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.inheritance.embedded.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(IntIdEntity.class)
public interface IntIdEntityView {

    @IdMapping
    public Integer getId();

    public String getName();
}
