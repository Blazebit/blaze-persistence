package com.blazebit.persistence.examples.spring.data.spqr.model;

import javax.persistence.Entity;

@Entity
public class Boy extends Child {
    public Boy(String name) {
        super(name);
    }

    public Boy() {
    }
}
