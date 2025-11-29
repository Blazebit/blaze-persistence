/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
@Embeddable
public class KeyedEmbeddable {

    @Column(name = "val", length = 10)
    private String value;
    @Column(length = 10)
    private String value2;

    public KeyedEmbeddable() {
    }

    public KeyedEmbeddable(String value, String value2) {
        this.value = value;
        this.value2 = value2;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }
}
