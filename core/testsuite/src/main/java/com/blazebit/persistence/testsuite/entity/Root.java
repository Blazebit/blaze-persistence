package com.blazebit.persistence.testsuite.entity;

import javax.persistence.*;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
@Entity(name = "Root")
public class Root {

    @Id
    private Integer id;

    @OneToMany
    @JoinTable(name = "list_one_to_many")
    @OrderColumn(name = "join_table_list_index")
    private List<IndexedNode> indexedNodes;
    @OneToMany
    @JoinTable(name = "map_one_to_many")
    @MapKeyColumn(name = "join_table_map_key", length = 10)
    private Map<String, KeyedNode> keyedNodes;

    @ManyToMany
    @JoinTable(name = "list_many_to_many")
    @OrderColumn(name = "join_table_list_index")
    private List<IndexedNode> indexedNodesMany;
    @ManyToMany
    @JoinTable(name = "map_many_to_many")
    @MapKeyColumn(name = "join_table_map_key", length = 10)
    private Map<String, KeyedNode> keyedNodesMany;

    @ManyToMany
    @JoinTable(name = "list_many_to_many_duplicate")
    @OrderColumn(name = "list_index")
    private List<IndexedNode> indexedNodesManyDuplicate;
    @ManyToMany
    @JoinTable(name = "map_many_to_many_duplicate")
    @MapKeyColumn(name = "map_key", length = 10)
    private Map<String, KeyedNode> keyedNodesManyDuplicate;

    @ElementCollection
    @CollectionTable(name = "list_collection_table")
    @OrderColumn(name = "list_index")
    private List<IndexedEmbeddable> indexedNodesElementCollection;
    @ElementCollection
    @CollectionTable(name = "map_collection_table")
    @MapKeyColumn(name = "map_key", length = 10)
    private Map<String, KeyedEmbeddable> keyedNodesElementCollection;

}
