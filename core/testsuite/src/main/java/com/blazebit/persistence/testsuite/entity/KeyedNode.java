package com.blazebit.persistence.testsuite.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 20.09.2016.
 */
@Entity(name = "KeyedNode")
public class KeyedNode {

    @Id
    private Integer id;
    @ManyToOne
    private Root parent;
    @Column(name = "map_key")
    private String key;
}
