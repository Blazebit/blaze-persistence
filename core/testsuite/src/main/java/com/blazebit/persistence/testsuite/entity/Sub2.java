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
@DiscriminatorValue("2")
public class Sub2 extends Parent {
    private static final long serialVersionUID = 1L;

    private Integer sub2Value;

    public Integer getSub2Value() {
        return sub2Value;
    }

    public void setSub2Value(Integer sub2Value) {
        this.sub2Value = sub2Value;
    }
}
