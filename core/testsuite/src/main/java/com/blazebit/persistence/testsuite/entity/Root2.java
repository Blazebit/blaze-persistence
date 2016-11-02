package com.blazebit.persistence.testsuite.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
@Entity(name = "Root2")
public class Root2 {

    @Id
    private Integer id;

    @OneToMany(mappedBy = "parent")
    @OrderColumn(name = "list_index")
    private List<IndexedNode2> indexedNodesMappedBy;
    @OneToMany(mappedBy = "parent")
    @MapKeyColumn(name = "map_key", length = 10)
    private Map<String, KeyedNode2> keyedNodesMappedBy;

}
