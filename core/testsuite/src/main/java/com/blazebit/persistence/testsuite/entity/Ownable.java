/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import java.io.Serializable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

/**
 *
 * @author Christian
 */
@MappedSuperclass
// NOTE: Datanucleus does not seem to support generic types of mapped super classes
public abstract class Ownable extends LongSequenceEntity /*SequenceBaseEntity<Long>*/ implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Person owner;

    public Ownable() {
    }

    public Ownable(Long id) {
        super(id);
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST })
    @JoinColumn(name = "owner_id")
    public Person getOwner() {
        return owner;
    }
    
    public void setOwner(Person owner) {
        this.owner = owner;
    }
}
