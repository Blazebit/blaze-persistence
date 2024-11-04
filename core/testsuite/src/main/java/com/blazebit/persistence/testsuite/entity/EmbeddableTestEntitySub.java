/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("sub")
public class EmbeddableTestEntitySub extends EmbeddableTestEntity {

    private static final long serialVersionUID = 1L;

}
