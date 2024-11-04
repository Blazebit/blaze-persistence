/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.testsuite.entity.TestEntity;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(TestEntity.class)
public interface TestEntityView extends NamedView {

    public String getDescription();
}
