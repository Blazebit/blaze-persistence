package com.blazebit.persistence.examples.spring.data.spqr.model;

import javax.persistence.Entity;

@Entity
public class Girl extends Child {

    private String dollName;

    public Girl(String name, String dollName) {
        super(name);
        this.dollName = dollName;
    }

    public Girl() {
    }

    public String getDollName() {
        return dollName;
    }

    public void setDollName(String dollName) {
        this.dollName = dollName;
    }
}
