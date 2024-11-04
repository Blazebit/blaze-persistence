/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.view.spring.views.sub2;

import com.blazebit.persistence.integration.view.spring.entity.TestEntity;
import com.blazebit.persistence.integration.view.spring.qualifier.TestEntityViewQualifier;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@TestEntityViewQualifier
@EntityView(TestEntity.class)
public interface TestView2 {

    @IdMapping
    public String getId();

    @Mapping("id + 2")
    public String getIdPlusTwo();

}
