/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import java.io.Serializable;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.ManyToOne;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Entity(name = "Projects")
@Inheritance
@DiscriminatorColumn(name = "category_type")
public abstract class Project<PL extends ProjectLeader<? extends Project<?>>> implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    private PL leader;

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = ProjectLeader.class)
    public PL getLeader() {
        return leader;
    }

    public void setLeader(PL leader) {
        this.leader = leader;
    }
}
