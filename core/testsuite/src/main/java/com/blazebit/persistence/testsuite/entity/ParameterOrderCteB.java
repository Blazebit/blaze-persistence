/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import com.blazebit.persistence.CTE;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import java.io.Serializable;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.1
 */
@Entity
@CTE
@IdClass(ParameterOrderCteB.ParameterOrderCteBId.class)
public class ParameterOrderCteB {

    public static class ParameterOrderCteBId implements Serializable {
        private Short seven;

        private Integer eight;

        private Long nine;

        public Short getSeven() {
            return seven;
        }

        public void setSeven(Short seven) {
            this.seven = seven;
        }

        public Integer getEight() {
            return eight;
        }

        public void setEight(Integer eight) {
            this.eight = eight;
        }

        public Long getNine() {
            return nine;
        }

        public void setNine(Long nine) {
            this.nine = nine;
        }
    }

    private Short seven;

    private Integer eight;

    private Long nine;

    @Id
    public Short getSeven() {
        return seven;
    }

    public void setSeven(Short seven) {
        this.seven = seven;
    }

    @Id
    public Integer getEight() {
        return eight;
    }

    public void setEight(Integer eight) {
        this.eight = eight;
    }

    @Id
    public Long getNine() {
        return nine;
    }

    public void setNine(Long nine) {
        this.nine = nine;
    }
}
