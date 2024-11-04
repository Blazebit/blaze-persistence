/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.quarkus.deployment.view;

import com.blazebit.persistence.integration.quarkus.deployment.entity.Person;
import com.blazebit.persistence.view.EntityView;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@EntityView(Person.class)
public interface PersonView extends PersonCreateView {
}
