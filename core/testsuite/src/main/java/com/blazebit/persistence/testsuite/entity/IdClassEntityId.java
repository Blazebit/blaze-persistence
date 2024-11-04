/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import java.io.Serializable;

/**
 * @author Christian Beikov
 * @since 1.3.0
 */
public class IdClassEntityId implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer key1;
    private String key2;

    public IdClassEntityId() {
    }

    public IdClassEntityId(Integer key1, String key2) {
        this.key1 = key1;
        this.key2 = key2;
    }

    public Integer getKey1() {
        return key1;
    }

    public void setKey1(Integer key1) {
        this.key1 = key1;
    }

    public String getKey2() {
        return key2;
    }

    public void setKey2(String key2) {
        this.key2 = key2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IdClassEntityId that = (IdClassEntityId) o;

        if (key1 != null ? !key1.equals(that.key1) : that.key1 != null) {
            return false;
        }
        return key2 != null ? key2.equals(that.key2) : that.key2 == null;
    }

    @Override
    public int hashCode() {
        int result = key1 != null ? key1.hashCode() : 0;
        result = 31 * result + (key2 != null ? key2.hashCode() : 0);
        return result;
    }
}
