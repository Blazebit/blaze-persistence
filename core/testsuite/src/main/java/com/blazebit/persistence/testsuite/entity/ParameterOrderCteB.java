package com.blazebit.persistence.testsuite.entity;

import com.blazebit.persistence.CTE;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@CTE
public class ParameterOrderCteB {

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
