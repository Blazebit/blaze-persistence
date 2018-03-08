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

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CTE;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.2.0
 */
public class Issue227Test extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
            RecursiveEntity.class,
            RecursiveEntityCte.class
        };
    }

    @Before
    public void setUp() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                RecursiveEntity root1 = new RecursiveEntity("root1");
                RecursiveEntity child1_1 = new RecursiveEntity("child1_1", root1);
                RecursiveEntity child1_2 = new RecursiveEntity("child1_2", root1);

                RecursiveEntity child1_1_1 = new RecursiveEntity("child1_1_1", child1_1);
                RecursiveEntity child1_2_1 = new RecursiveEntity("child1_2_1", child1_2);

                Set<RecursiveEntity> set = new HashSet<RecursiveEntity>();
                set.add(root1);

                root1.setManyToManyChildren(set);
                child1_1.setManyToManyChildren(set);
                child1_2.setManyToManyChildren(set);
                child1_1_1.setManyToManyChildren(set);
                child1_2_1.setManyToManyChildren(set);

                em.persist(root1);
                em.persist(child1_1);
                em.persist(child1_2);
                em.persist(child1_1_1);
                em.persist(child1_2_1);
            }
        });
    }

    @Test
    // NOTE: MySQL has no CTE support
    // TODO: Oracle requires a cycle clause #295
    @Category({ NoMySQL.class, NoOracle.class })
    public void testFetchModeSubselectOnCteQueryResult() throws Exception {
        CriteriaBuilder<RecursiveEntityCte> cb = cbf.create(em, RecursiveEntityCte.class)
            .withRecursive(RecursiveEntityCte.class)
                .from(RecursiveEntity.class, "recEntity")
                .bind("id").select("recEntity.id")
                .bind("name").select("recEntity.name")
                .bind("parent").select("recEntity.parent")
                .where("recEntity.parent").isNotNull()
            .unionAll()
                .from(RecursiveEntity.class, "recEntity")
                .from(RecursiveEntityCte.class, "parentRecEntity")
                .bind("id").select("recEntity.id")
                .bind("name").select("recEntity.name")
                .bind("parent").select("recEntity.parent")
                .where("recEntity.id").eqExpression("parentRecEntity.parent.id")
            .end()
            .fetch("parent")
            .orderByAsc("name");

        List<RecursiveEntityCte> result = cb.getResultList();
        RecursiveEntity firstParent = result.get(0).getParent();
        Assert.assertEquals(2, firstParent.getChildren().size());
        Assert.assertEquals(1, firstParent.getManyToManyChildren().size());
    }

    /**
     * Altered version of {@link RecursiveEntity} that has
     * a {@link FetchMode#SUBSELECT} on the {@link RecursiveEntity#children} relationship.
     */
    @Entity
    @Table(name = "issue_227_recursive_entity")
    public static class RecursiveEntity {

        private Long id;
        private String name;
        private RecursiveEntity parent;
        private Set<RecursiveEntity> children = new HashSet<RecursiveEntity>(0);
        private Set<RecursiveEntity> manyToManyChildren = new HashSet<RecursiveEntity>(0);

        public RecursiveEntity() {
        }

        public RecursiveEntity(String name) {
            this.name = name;
        }

        public RecursiveEntity(String name, RecursiveEntity parent) {
            this.name = name;
            this.parent = parent;
        }

        @Id
        @GeneratedValue
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        @Basic(optional = false)
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @JoinColumn(name = "parent_id")
        @ManyToOne(fetch = FetchType.LAZY)
        public RecursiveEntity getParent() {
            return parent;
        }

        public void setParent(RecursiveEntity parent) {
            this.parent = parent;
        }

        /*
         * This is what the test is about, uncomment the FetchMode here, and the test passes.
         */
        @Fetch(FetchMode.SUBSELECT)
        @OneToMany(mappedBy = "parent")
        public Set<RecursiveEntity> getChildren() {
            return children;
        }

        public void setChildren(Set<RecursiveEntity> children) {
            this.children = children;
        }

        @Fetch(FetchMode.SUBSELECT)
        @ManyToMany
        @JoinTable(name = "many_to_many_children", joinColumns = {
            @JoinColumn(name = "other_scope_id", nullable = false)
        }, inverseJoinColumns = {
            @JoinColumn(name = "scope_id", nullable = false)
        })
        public Set<RecursiveEntity> getManyToManyChildren() {
            return manyToManyChildren;
        }

        public void setManyToManyChildren(Set<RecursiveEntity> manyToManyChildren) {
            this.manyToManyChildren = manyToManyChildren;
        }
    }

    @CTE
    @Entity(name = "RecursiveEntityCte")
    public static class RecursiveEntityCte {

        private Long id;
        private String name;
        private RecursiveEntity parent;

        @Id
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

        @ManyToOne
        public RecursiveEntity getParent() { return parent; }
        public void setParent(RecursiveEntity parent) { this.parent = parent; }

    }

}
