/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SecondaryTable;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@SecondaryTable(name = "test")
public class SecondaryTableEntitySub extends SecondaryTableEntityBase {

    private Long b;

    private Long c;

    @Column
    public Long getB() {
        return b;
    }

    public void setB(Long b) {
        this.b = b;
    }

    @Column(table = "test")
    public Long getC() {
        return c;
    }

    public void setC(Long c) {
        this.c = c;
    }
}
