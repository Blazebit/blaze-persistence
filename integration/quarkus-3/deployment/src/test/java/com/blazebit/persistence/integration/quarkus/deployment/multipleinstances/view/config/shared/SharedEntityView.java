/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.quarkus.deployment.multipleinstances.view.config.shared;

import com.blazebit.persistence.integration.quarkus.deployment.multipleinstances.entity.SharedEntity;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

/**
 * @author Moritz Becker
 * @since 1.6.0
 */
@EntityView(SharedEntity.class)
public interface SharedEntityView {

    @IdMapping
    Long getId();

    String getName();
}
