/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.testsuite.entity;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.6.8
 */
@Entity
@Table(name = "prim_fam")
public class PrimitiveFamily implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String name;
    private PrimitivePerson person;

    public PrimitiveFamily() {
    }

    public PrimitiveFamily(String name) {
        this.name = name;
    }

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic(optional = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    public PrimitivePerson getPerson() {
        return person;
    }

    public void setPerson(PrimitivePerson person) {
        this.person = person;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PrimitiveFamily)) {
            return false;
        }

        PrimitiveFamily that = (PrimitiveFamily) o;

        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return (int) (getId() ^ (getId() >>> 32));
    }
}
