/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.view.cdi;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(TestEntity.class)
public interface TestView {

    @IdMapping
    public String getId();
}
