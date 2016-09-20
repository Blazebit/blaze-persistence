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
@Entity(name = "KeyedNode2")
public class KeyedNode2 {

    @Id
    private Integer id;
    @ManyToOne
    private Root2 parent;
    @Column(name = "map_key")
    private String key;
}
