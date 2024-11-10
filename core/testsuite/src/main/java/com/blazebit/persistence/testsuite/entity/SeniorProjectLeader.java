/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Entity
@DiscriminatorValue("S")
public class SeniorProjectLeader extends ProjectLeader<LargeProject> {

    private static final long serialVersionUID = 1L;
    
}
