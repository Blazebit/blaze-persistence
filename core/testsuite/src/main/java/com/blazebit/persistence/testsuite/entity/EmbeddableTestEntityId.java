/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class EmbeddableTestEntityId implements Serializable {

    private static final long serialVersionUID = 1L;

    private String value;
    private String key;

    public EmbeddableTestEntityId() {
    }

    public EmbeddableTestEntityId(String value, String key) {
        this.value = value;
        this.key = key;
    }

    // Fixed size because mysql has size limitations
    @Column(name = "test_value", nullable = false, length = 10)
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    // Rename because mysql can't handle "key"
    // Fixed size because mysql has size limitations
    @Column(name = "test_key", nullable = false, length = 100)
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EmbeddableTestEntityId)) {
            return false;
        }

        EmbeddableTestEntityId that = (EmbeddableTestEntityId) o;

        if (getValue() != null ? !getValue().equals(that.getValue()) : that.getValue() != null) {
            return false;
        }
        return getKey() != null ? getKey().equals(that.getKey()) : that.getKey() == null;

    }

    @Override
    public int hashCode() {
        int result = getValue() != null ? getValue().hashCode() : 0;
        result = 31 * result + (getKey() != null ? getKey().hashCode() : 0);
        return result;
    }
}
