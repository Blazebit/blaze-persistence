/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import javax.persistence.Entity;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.1
 */
@Entity
public class ParameterOrderEntity extends LongSequenceEntity {

    private Short one;

    private Integer two;

    private Long three;

    public Short getOne() {
        return one;
    }

    public void setOne(Short one) {
        this.one = one;
    }

    public Integer getTwo() {
        return two;
    }

    public void setTwo(Integer two) {
        this.two = two;
    }

    public Long getThree() {
        return three;
    }

    public void setThree(Long three) {
        this.three = three;
    }
}
