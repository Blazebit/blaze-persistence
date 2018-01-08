/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.testsuite.entity;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
    @MapKeyColumn(name = "join_table_map_key1", length = 10)
    private Map<String, KeyedNode> keyedNodes;

    @ManyToMany
    @JoinTable(name = "list_many_to_many")
    @OrderColumn(name = "join_table_list_index")
    private List<IndexedNode> indexedNodesMany;
    @ManyToMany
    @JoinTable(name = "map_many_to_many")
    @MapKeyColumn(name = "join_table_map_key2", length = 10)
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
