/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.subview.multiplecollections.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author Moritz Becker
 * @since 1.4.0
 */
@Entity
@IdClass(C.CompositeId.class)
public class C {
    @Id
    private Long id1;
    @Id
    private Long id2;
    @Column(name = "val")
    private String value;

    public C() { }

    public C(Long id1, Long id2) {
        this.id1 = id1;
        this.id2 = id2;
    }

    public Long getId1() {
        return id1;
    }

    public void setId1(Long id1) {
        this.id1 = id1;
    }

    public Long getId2() {
        return id2;
    }

    public void setId2(Long id2) {
        this.id2 = id2;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof C)) return false;
        C c = (C) o;
        return Objects.equals(getId1(), c.getId1()) &&
                Objects.equals(getId2(), c.getId2());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId1(), getId2());
    }

    public static class CompositeId implements Serializable {
        private Long id1;
        private Long id2;

        public Long getId1() {
            return id1;
        }

        public void setId1(Long id1) {
            this.id1 = id1;
        }

        public Long getId2() {
            return id2;
        }

        public void setId2(Long id2) {
            this.id2 = id2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CompositeId)) return false;
            CompositeId that = (CompositeId) o;
            return Objects.equals(getId1(), that.getId1()) &&
                    Objects.equals(getId2(), that.getId2());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getId1(), getId2());
        }
    }
}
