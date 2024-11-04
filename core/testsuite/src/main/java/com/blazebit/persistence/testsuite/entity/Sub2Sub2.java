/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@Entity
@DiscriminatorValue("22")
public class Sub2Sub2 extends Sub2 {
    private static final long serialVersionUID = 1L;

}
