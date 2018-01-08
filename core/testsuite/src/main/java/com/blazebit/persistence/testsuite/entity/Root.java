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
import java.util.ArrayList;
import java.util.HashMap;
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
    private String name;

    @OneToMany
    @JoinTable(name = "list_one_to_many")
    @OrderColumn(name = "join_table_list_index")
    private List<IndexedNode> indexedNodes = new ArrayList<>();
    @OneToMany
    @JoinTable(name = "map_one_to_many")
    @MapKeyColumn(name = "join_table_map_key1", length = 10)
    private Map<String, KeyedNode> keyedNodes = new HashMap<>();

    @ManyToMany
    @JoinTable(name = "list_many_to_many")
    @OrderColumn(name = "join_table_list_index")
    private List<IndexedNode> indexedNodesMany = new ArrayList<>();
    @ManyToMany
    @JoinTable(name = "map_many_to_many")
    @MapKeyColumn(name = "join_table_map_key2", length = 10)
    private Map<String, KeyedNode> keyedNodesMany = new HashMap<>();

    @ManyToMany
    @JoinTable(name = "list_many_to_many_duplicate")
    @OrderColumn(name = "list_index")
    private List<IndexedNode> indexedNodesManyDuplicate = new ArrayList<>();
    @ManyToMany
    @JoinTable(name = "map_many_to_many_duplicate")
    @MapKeyColumn(name = "map_key", length = 10)
    private Map<String, KeyedNode> keyedNodesManyDuplicate = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "list_collection_table")
    @OrderColumn(name = "list_index")
    private List<IndexedEmbeddable> indexedNodesElementCollection = new ArrayList<>();
    @ElementCollection
    @CollectionTable(name = "map_collection_table")
    @MapKeyColumn(name = "map_key", length = 10)
    private Map<String, KeyedEmbeddable> keyedNodesElementCollection = new HashMap<>();

    public Root() {
    }

    public Root(Integer id) {
        this.id = id;
    }

    public Root(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<IndexedNode> getIndexedNodes() {
        return indexedNodes;
    }

    public void setIndexedNodes(List<IndexedNode> indexedNodes) {
        this.indexedNodes = indexedNodes;
    }

    public Map<String, KeyedNode> getKeyedNodes() {
        return keyedNodes;
    }

    public void setKeyedNodes(Map<String, KeyedNode> keyedNodes) {
        this.keyedNodes = keyedNodes;
    }

    public List<IndexedNode> getIndexedNodesMany() {
        return indexedNodesMany;
    }

    public void setIndexedNodesMany(List<IndexedNode> indexedNodesMany) {
        this.indexedNodesMany = indexedNodesMany;
    }

    public Map<String, KeyedNode> getKeyedNodesMany() {
        return keyedNodesMany;
    }

    public void setKeyedNodesMany(Map<String, KeyedNode> keyedNodesMany) {
        this.keyedNodesMany = keyedNodesMany;
    }

    public List<IndexedNode> getIndexedNodesManyDuplicate() {
        return indexedNodesManyDuplicate;
    }

    public void setIndexedNodesManyDuplicate(List<IndexedNode> indexedNodesManyDuplicate) {
        this.indexedNodesManyDuplicate = indexedNodesManyDuplicate;
    }

    public Map<String, KeyedNode> getKeyedNodesManyDuplicate() {
        return keyedNodesManyDuplicate;
    }

    public void setKeyedNodesManyDuplicate(Map<String, KeyedNode> keyedNodesManyDuplicate) {
        this.keyedNodesManyDuplicate = keyedNodesManyDuplicate;
    }

    public List<IndexedEmbeddable> getIndexedNodesElementCollection() {
        return indexedNodesElementCollection;
    }

    public void setIndexedNodesElementCollection(List<IndexedEmbeddable> indexedNodesElementCollection) {
        this.indexedNodesElementCollection = indexedNodesElementCollection;
    }

    public Map<String, KeyedEmbeddable> getKeyedNodesElementCollection() {
        return keyedNodesElementCollection;
    }

    public void setKeyedNodesElementCollection(Map<String, KeyedEmbeddable> keyedNodesElementCollection) {
        this.keyedNodesElementCollection = keyedNodesElementCollection;
    }
}
