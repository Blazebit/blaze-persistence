/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import com.blazebit.persistence.CTE;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.1
 */
@Entity
@CTE
public class ParameterOrderCte {

    private Short four;

    private Integer five;

    private Long six;

    @Id
    public Short getFour() {
        return four;
    }

    public void setFour(Short four) {
        this.four = four;
    }

    public Integer getFive() {
        return five;
    }

    public void setFive(Integer five) {
        this.five = five;
    }

    public Long getSix() {
        return six;
    }

    public void setSix(Long six) {
        this.six = six;
    }
}
