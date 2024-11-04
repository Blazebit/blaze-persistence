/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Entity
public class KeysetEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer a;
    private Integer b;

    public KeysetEntity() {
    }

    public KeysetEntity(Integer id, Integer a, Integer b) {
        this.id = id;
        this.a = a;
        this.b = b;
    }

    @Id
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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
}
