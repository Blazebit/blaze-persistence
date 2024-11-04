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
@DiscriminatorValue("J")
public class JuniorProjectLeader extends ProjectLeader<SmallProject> {

    private static final long serialVersionUID = 1L;
    
}
