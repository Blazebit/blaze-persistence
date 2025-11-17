/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.io.Serializable;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
@Entity
public class KeysetEntity2 implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer a;
    private Integer b;
    private Integer c;

    public KeysetEntity2() {
    }

    public KeysetEntity2(Integer id, Integer a, Integer b, Integer c) {
        this.id = id;
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Id
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Basic(optional = false)
    public Integer getA() {
        return a;
    }

    public void setA(Integer a) {
        this.a = a;
    }

    public Integer getB() {
        return b;
    }

    public void setB(Integer b) {
        this.b = b;
    }

    public Integer getC() {
        return c;
    }

    public void setC(Integer c) {
        this.c = c;
    }
}
