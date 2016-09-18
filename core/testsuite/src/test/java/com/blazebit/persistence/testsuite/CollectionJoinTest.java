/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.category.NoHibernate;
import com.blazebit.persistence.testsuite.base.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.Document;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.*;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CollectionJoinTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            Root.class,
            IndexedNode.class,
            KeyedNode.class,
            KeyedEmbeddable.class,
            IndexedEmbeddable.class
        };
    }

    @Test
    public void testOneToManyJoinTable() {
        CriteriaBuilder<Root> criteria = cbf.create(em, Root.class, "r");
        criteria.select("r.indexedNodes[0]");
        criteria.select("r.keyedNodes['default']");

        assertEquals("SELECT " + joinAliasValue("indexedNodes_0_1") + ", " + joinAliasValue("keyedNodes_default_1") + " FROM Root r " +
                "LEFT JOIN r.indexedNodes indexedNodes_0_1 " + ON_CLAUSE + " INDEX(indexedNodes_0_1) = 0 " +
                "LEFT JOIN r.keyedNodes keyedNodes_default_1 " + ON_CLAUSE + " KEY(keyedNodes_default_1) = 'default'", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testOneToManyMappedBy() {
        CriteriaBuilder<Root> criteria = cbf.create(em, Root.class, "r");
        criteria.select("r.indexedNodesMappedBy[0]");
        criteria.select("r.keyedNodesMappedBy['default']");

        assertEquals("SELECT " + joinAliasValue("indexedNodesMappedBy_0_1") + ", " + joinAliasValue("keyedNodesMappedBy_default_1") + " FROM Root r " +
                "LEFT JOIN r.indexedNodesMappedBy indexedNodesMappedBy_0_1 " + ON_CLAUSE + " INDEX(indexedNodesMappedBy_0_1) = 0 " +
                "LEFT JOIN r.keyedNodesMappedBy keyedNodesMappedBy_default_1 " + ON_CLAUSE + " KEY(keyedNodesMappedBy_default_1) = 'default'", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testManyToManyJoinTable() {
        CriteriaBuilder<Root> criteria = cbf.create(em, Root.class, "r");
        criteria.select("r.indexedNodesMany[0]");
        criteria.select("r.keyedNodesMany['default']");

        assertEquals("SELECT " + joinAliasValue("indexedNodesMany_0_1") + ", " + joinAliasValue("keyedNodesMany_default_1") + " FROM Root r " +
                "LEFT JOIN r.indexedNodesMany indexedNodesMany_0_1 " + ON_CLAUSE + " INDEX(indexedNodesMany_0_1) = 0 " +
                "LEFT JOIN r.keyedNodesMany keyedNodesMany_default_1 " + ON_CLAUSE + " KEY(keyedNodesMany_default_1) = 'default'", criteria.getQueryString());
        criteria.getResultList();
    }

    // Normally we would have duplicate names, but since we are re-aliasing the parent id and collection key names, this must pass
    @Test
    public void testManyToManyJoinTableDuplicateName() {
        CriteriaBuilder<Root> criteria = cbf.create(em, Root.class, "r");
        criteria.select("r.indexedNodesManyDuplicate[0]");
        criteria.select("r.keyedNodesManyDuplicate['default']");

        assertEquals("SELECT " + joinAliasValue("indexedNodesManyDuplicate_0_1") + ", " + joinAliasValue("keyedNodesManyDuplicate_default_1") + " FROM Root r " +
                "LEFT JOIN r.indexedNodesManyDuplicate indexedNodesManyDuplicate_0_1 " + ON_CLAUSE + " INDEX(indexedNodesManyDuplicate_0_1) = 0 " +
                "LEFT JOIN r.keyedNodesManyDuplicate keyedNodesManyDuplicate_default_1 " + ON_CLAUSE + " KEY(keyedNodesManyDuplicate_default_1) = 'default'", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // Hibernate bug
    @Category({ NoHibernate.class })
    public void testElementCollection() {
        CriteriaBuilder<Root> criteria = cbf.create(em, Root.class, "r");
        criteria.select("r.indexedNodesElementCollection[0]");
        criteria.select("r.keyedNodesElementCollection['default']");

        assertEquals("SELECT " + joinAliasValue("indexedNodesElementCollection_0_1") + ", " + joinAliasValue("keyedNodesElementCollection_default_1") + " FROM Root r " +
                "LEFT JOIN r.indexedNodesElementCollection indexedNodesElementCollection_0_1 " + ON_CLAUSE + " INDEX(indexedNodesElementCollection_0_1) = 0 " +
                "LEFT JOIN r.keyedNodesElementCollection keyedNodesElementCollection_default_1 " + ON_CLAUSE + " KEY(keyedNodesElementCollection_default_1) = 'default'", criteria.getQueryString());
        criteria.getResultList();
    }

    @Entity(name = "Root")
    static class Root {

        @Id
        private Integer id;

        @OneToMany
        @JoinTable(name = "list_one_to_many")
        @OrderColumn(name = "join_table_list_index")
        private List<IndexedNode> indexedNodes;
        @OneToMany
        @JoinTable(name = "map_one_to_many")
        @MapKeyColumn(name = "join_table_map_key")
        private Map<String, KeyedNode> keyedNodes;

        @OneToMany(mappedBy = "parent")
        @OrderColumn(name = "list_index")
        private List<IndexedNode> indexedNodesMappedBy;
        @OneToMany(mappedBy = "parent")
        @MapKeyColumn(name = "map_key")
        private Map<String, KeyedNode> keyedNodesMappedBy;

        @ManyToMany
        @JoinTable(name = "list_many_to_many")
        @OrderColumn(name = "join_table_list_index")
        private List<IndexedNode> indexedNodesMany;
        @ManyToMany
        @JoinTable(name = "map_many_to_many")
        @MapKeyColumn(name = "join_table_map_key")
        private Map<String, KeyedNode> keyedNodesMany;

        @ManyToMany
        @JoinTable(name = "list_many_to_many_duplicate")
        @OrderColumn(name = "list_index")
        private List<IndexedNode> indexedNodesManyDuplicate;
        @ManyToMany
        @JoinTable(name = "map_many_to_many_duplicate")
        @MapKeyColumn(name = "map_key")
        private Map<String, KeyedNode> keyedNodesManyDuplicate;

        @ElementCollection
        @CollectionTable(name = "list_collection_table")
        @OrderColumn(name = "list_index")
        private List<IndexedEmbeddable> indexedNodesElementCollection;
        @ElementCollection
        @CollectionTable(name = "map_collection_table")
        @MapKeyColumn(name = "map_key")
        private Map<String, KeyedEmbeddable> keyedNodesElementCollection;

    }

    @Entity(name = "IndexedNode")
    static class IndexedNode {

        @Id
        private Integer id;
        @ManyToOne
        private Root parent;
        @Column(name = "list_index")
        private Integer index;
    }

    @Entity(name = "KeyedNode")
    static class KeyedNode {

        @Id
        private Integer id;
        @ManyToOne
        private Root parent;
        @Column(name = "map_key")
        private String key;
    }

    @Embeddable
    static class IndexedEmbeddable {

        private String value;
    }

    @Embeddable
    static class KeyedEmbeddable {

        private String value;
    }
}
