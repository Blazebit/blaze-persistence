/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import javax.persistence.Entity;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Entity
public class TPCSub1 extends TPCBase {
    private static final long serialVersionUID = 1L;

    private String name;
    private int sub1;

    public TPCSub1() {
    }

    public TPCSub1(Long id, String base) {
        super(id, base);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSub1() {
        return sub1;
    }

    public void setSub1(int sub1) {
        this.sub1 = sub1;
    }
}
