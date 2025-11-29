/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    @JoinTable(name = "list_one_to_many_set")
    private Set<Parent> nodes = new HashSet<>();
    @ManyToMany
    @JoinTable(name = "set_one_to_many_poly", joinColumns = @JoinColumn(name = "root_id"), inverseJoinColumns = @JoinColumn(name = "poly_id"))
    private Set<Sub1> nodesPoly = new HashSet<>();

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

    public Set<Parent> getNodes() {
        return nodes;
    }

    public void setNodes(Set<Parent> nodes) {
        this.nodes = nodes;
    }

    public Set<Sub1> getNodesPoly() {
        return nodesPoly;
    }

    public void setNodesPoly(Set<Sub1> nodesPoly) {
        this.nodesPoly = nodesPoly;
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
