/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("sub")
public class EmbeddableTestEntitySub extends EmbeddableTestEntity {

    private static final long serialVersionUID = 1L;

}
