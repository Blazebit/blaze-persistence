/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.view.spring.views.sub1;

import com.blazebit.persistence.integration.view.spring.entity.TestEntity;
import com.blazebit.persistence.integration.view.spring.views.SomeSuperInterface;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@EntityView(TestEntity.class)
public interface TestView1 extends SomeSuperInterface {

    @IdMapping
    public String getId();

}
