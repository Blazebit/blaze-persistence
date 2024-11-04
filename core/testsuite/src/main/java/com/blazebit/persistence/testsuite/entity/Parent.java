/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@Entity
@Table(name = "PARENT_TBL")
@DiscriminatorColumn(name = "DTYPE", discriminatorType = DiscriminatorType.INTEGER, length = 0)
@DiscriminatorValue("0")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Parent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Integer number;

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "order_nr")
    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

}
