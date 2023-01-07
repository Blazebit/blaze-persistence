package com.blazebit.persistence.examples.spring.data.spqr.model;

import javax.persistence.Entity;

@Entity
public class Girl extends Child {
    public Girl(String name) {
        super(name);
    }

    public Girl() {
    }
}
