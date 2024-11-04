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
public class TPCSub2 extends TPCBase {
    private static final long serialVersionUID = 1L;

    private String name;
    private int sub2;

    public TPCSub2() {
    }

    public TPCSub2(Long id, String base) {
        super(id, base);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSub2() {
        return sub2;
    }

    public void setSub2(int sub2) {
        this.sub2 = sub2;
    }
}
