/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.quarkus.deployment.multipleinstances.view.config.desk;

import com.blazebit.persistence.integration.quarkus.deployment.multipleinstances.entity.Desk;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

/**
 * @author Moritz Becker
 * @since 1.6.0
 */
@EntityView(Desk.class)
public interface DeskView {

    @IdMapping
    Long getId();

    String getName();
}
