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
@DiscriminatorValue("11")
public class Sub1Sub2 extends Sub1 {
    private static final long serialVersionUID = 1L;

}
