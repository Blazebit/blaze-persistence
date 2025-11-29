/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.treat.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class IntValueEmbeddable implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer someValue;

    public IntValueEmbeddable() {
    }

    public IntValueEmbeddable(Integer someValue) {
        this.someValue = someValue;
    }

    public Integer getSomeValue() {
        return someValue;
    }

    public void setSomeValue(Integer someValue) {
        this.someValue = someValue;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.someValue);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IntValueEmbeddable other = (IntValueEmbeddable) obj;
        if (!Objects.equals(this.someValue, other.someValue)) {
            return false;
        }
        return true;
    }
}
