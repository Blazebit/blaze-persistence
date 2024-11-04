/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.quarkus.deployment.multipleinstances.view.annotation.car;

import com.blazebit.persistence.integration.quarkus.deployment.multipleinstances.entity.Car;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

/**
 * @author Moritz Becker
 * @since 1.6.0
 */
@EntityView(Car.class)
public interface CarView {

    @IdMapping
    Long getId();

    String getName();
}
