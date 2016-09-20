package com.blazebit.persistence.testsuite.entity;

import javax.persistence.*;
import java.util.List;
import java.util.Map;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 20.09.2016.
 */
@Entity(name = "Root2")
public class Root2 {

    @Id
    private Integer id;

    @OneToMany(mappedBy = "parent")
    @OrderColumn(name = "list_index")
    private List<IndexedNode2> indexedNodesMappedBy;
    @OneToMany(mappedBy = "parent")
    @MapKeyColumn(name = "map_key")
    private Map<String, KeyedNode2> keyedNodesMappedBy;

}
