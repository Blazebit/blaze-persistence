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
 * @since 1.4.0
 */
@Entity
@DiscriminatorValue("1")
public class Sub1 extends Parent {
    private static final long serialVersionUID = 1L;

    private Integer sub1Value;

    public Integer getSub1Value() {
        return sub1Value;
    }

    public void setSub1Value(Integer sub1Value) {
        this.sub1Value = sub1Value;
    }
}
