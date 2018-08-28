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
import com.blazebit.persistence.impl.query.CustomSQLTypedQuery;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.entity.IdClassEntity;
import com.blazebit.persistence.testsuite.entity.IdClassEntityId;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

/**
 * @since 1.3.0
 * @author Jan-Willem Gmelig Meyling
 */
public class Issue571Test extends AbstractCoreTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] { Cte.class, MyEntity.class, IdClassEntity.class, IdClassReferencingIdClass.class, EntityWithNestedIdClass.class };
    }

    @Before
    public void setUp() throws Exception {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                MyEntity myEntity = new MyEntity();
                myEntity.setAttribute("attr");
                em.persist(myEntity);
            }
        });
    }

    @Test
    // Ignore MySQL / Oracle because of unsuppored use of CTE's, ignore Hibernate 4.3 and 4.3 metamodel bug
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class, NoOracle.class, NoHibernate42.class, NoHibernate43.class })
    public void testBindingCteAssociationToEntity() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                CriteriaBuilder<MyEntity> select = cbf.create(em, MyEntity.class)
                        .withRecursive(Cte.class)
                        .from(MyEntity.class, "a")
                        .bind("myEntity").select("a")
                        .bind("id").select("1")
                        .unionAll()
                        .from(MyEntity.class, "a")
                        .where("a.attribute").eq("bogus")
                        .bind("myEntity").select("a")
                        .bind("id").select("1")
                        .end()
                        .from(Cte.class)
                        .select("myEntity");

                select.getResultList();

                String sql = ((CustomSQLTypedQuery) select.getQuery()).getQuerySpecification().getSql();
                String cteBase = sql.substring(sql.indexOf("\nselect") + 1, sql.lastIndexOf("UNION ALL") - 1);
                String cteRecursivePart = sql.substring(sql.indexOf("UNION ALL")+10, sql.lastIndexOf("select")-3);
                String queryPart = sql.substring(sql.lastIndexOf("select"));

                assertEquals("Recursive base part should project 2 columns separated by a single comma",
                        cteBase.indexOf(","), cteBase.lastIndexOf(","));

                assertEquals("Recursive part should project 2 columns separated by a single comma",
                        cteRecursivePart.indexOf(","), cteRecursivePart.lastIndexOf(","));

                assertEquals("Final query should project 2 columns separated by a single comma",
                        queryPart.indexOf(","), queryPart.lastIndexOf(","));
            }
        });
    }

    @Test
    // Ignore MySQL / Oracle because of unsuppored use of CTE's, ignore Hibernate 4.3 and 4.3 metamodel bug
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class, NoOracle.class, NoHibernate42.class, NoHibernate43.class  })
    public void testBindingCteAssociationToEntityId() {
        expectedException.expectMessage("An association should be bound to its association type and not its identifier type");

        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                CriteriaBuilder<MyEntity> select = cbf.create(em, MyEntity.class)
                        .withRecursive(Cte.class)
                        .from(MyEntity.class, "a")
                        .bind("myEntity").select("a.id")
                        .bind("id").select("1")
                        .unionAll()
                        .from(MyEntity.class, "a")
                        .where("a.attribute").eq("bogus")
                        .bind("myEntity").select("a.id")
                        .bind("id").select("1")
                        .end()
                        .from(Cte.class)
                        .select("myEntity");

                select.getResultList();

                String sql = ((CustomSQLTypedQuery) select.getQuery()).getQuerySpecification().getSql();
                String cteBase = sql.substring(sql.indexOf("\nselect") + 1, sql.lastIndexOf("UNION ALL") - 1);
                String cteRecursivePart = sql.substring(sql.indexOf("UNION ALL")+10, sql.lastIndexOf("select")-3);
                String queryPart = sql.substring(sql.lastIndexOf("select"));

                assertEquals("Recursive base part should project 2 columns separated by a single comma",
                        cteBase.indexOf(","), cteBase.lastIndexOf(","));

                assertEquals("Recursive part should project 2 columns separated by a single comma",
                        cteRecursivePart.indexOf(","), cteRecursivePart.lastIndexOf(","));

                assertEquals("Final query should project 2 columns separated by a single comma",
                        queryPart.indexOf(","), queryPart.lastIndexOf(","));

            }
        });
    }

    @Test
    // Ignore MySQL / Oracle because of unsuppored use of CTE's, ignore Hibernate 4.3 and 4.3 metamodel bug
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class, NoOracle.class, NoHibernate42.class, NoHibernate43.class  })
    public void testBindingCteAssociationIdToEntityId() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                CriteriaBuilder<MyEntity> select = cbf.create(em, MyEntity.class)
                        .withRecursive(Cte.class)
                        .from(MyEntity.class, "a")
                        .bind("myEntity.id").select("a.id")
                        .bind("id").select("1")
                        .unionAll()
                        .from(MyEntity.class, "a")
                        .where("a.attribute").eq("bogus")
                        .bind("myEntity.id").select("a.id")
                        .bind("id").select("1")
                        .end()
                        .from(Cte.class)
                        .select("myEntity");

                select.getResultList();

                String sql = ((CustomSQLTypedQuery) select.getQuery()).getQuerySpecification().getSql();
                String cteBase = sql.substring(sql.indexOf("\nselect") + 1, sql.lastIndexOf("UNION ALL") - 1);
                String cteRecursivePart = sql.substring(sql.indexOf("UNION ALL")+10, sql.lastIndexOf("select")-3);
                String queryPart = sql.substring(sql.lastIndexOf("select"));

                assertEquals("Recursive base part should project 2 columns separated by a single comma",
                        cteBase.indexOf(","), cteBase.lastIndexOf(","));

                assertEquals("Recursive part should project 2 columns separated by a single comma",
                        cteRecursivePart.indexOf(","), cteRecursivePart.lastIndexOf(","));

                assertEquals("Final query should project 2 columns separated by a single comma",
                        queryPart.indexOf(","), queryPart.lastIndexOf(","));

            }
        });
    }

    @Test
    // Ignore MySQL / Oracle because of unsuppored use of CTE's, ignore Hibernate 4.3 and 4.3 metamodel bug
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class, NoOracle.class, NoHibernate42.class, NoHibernate43.class  })
    public void testBindingCteUsingNestedIdClass() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                CriteriaBuilder<IdClassEntity> select = cbf.create(em, IdClassEntity.class)
                        .with(IdClassReferencingIdClass.class)
                        .from(EntityWithNestedIdClass.class, "a")
                        .bind("id").select("a.idClassEntity.value")
                        .bind("idClassEntity").select("a")
                        .bind("someOtherCol").select("'str'")
                        .end()
                        .from(IdClassReferencingIdClass.class, "b")
                        .select("b.idClassEntity");

                select.getResultList();
            }
        });
    }

    @CTE
    @Entity
    @IdClass(Cte.CteIdClass.class)
    public static class Cte {

        @Id private Long id;
        @Id @ManyToOne private MyEntity myEntity;

        public MyEntity getMyEntity() {
            return myEntity;
        }

        public void setMyEntity(MyEntity myEntity) {
            this.myEntity = myEntity;
        }

        public static class CteIdClass implements Serializable {

            Long id;
            Long myEntity;

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                CteIdClass that = (CteIdClass) o;
                return Objects.equals(id, that.id) &&
                        Objects.equals(myEntity, that.myEntity);
            }

            @Override
            public int hashCode() {

                return Objects.hash(id, myEntity);
            }
        }

    }

    @Entity
    @IdClass(EntityWithNestedIdClass.EntityWithNestedIdClassId.class)
    public static class EntityWithNestedIdClass {

        @Id private Long id;
        @Id @ManyToOne private IdClassEntity idClassEntity;

        public static class EntityWithNestedIdClassId implements Serializable {

            private Long id;
            private IdClassEntityId idClassEntity;

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                EntityWithNestedIdClassId that = (EntityWithNestedIdClassId) o;
                return Objects.equals(id, that.id) &&
                        Objects.equals(idClassEntity, that.idClassEntity);
            }

            @Override
            public int hashCode() {

                return Objects.hash(id, idClassEntity);
            }
        }
    }

    @CTE
    @Entity
    @IdClass(IdClassReferencingIdClass.IdClassReferencingIdClassId.class)
    public static class IdClassReferencingIdClass {

        @Id private Long id;
        @Id @ManyToOne private EntityWithNestedIdClass idClassEntity;
        private String someOtherCol;

        public static class IdClassReferencingIdClassId implements Serializable {

            private Long id;
            private EntityWithNestedIdClass.EntityWithNestedIdClassId idClassEntity;

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                IdClassReferencingIdClassId that = (IdClassReferencingIdClassId) o;
                return Objects.equals(id, that.id) &&
                        Objects.equals(idClassEntity, that.idClassEntity);
            }

            @Override
            public int hashCode() {
                return Objects.hash(id, idClassEntity);
            }
        }
    }

    @Entity
    public static class MyEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String attribute;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getAttribute() {
            return attribute;
        }

        public void setAttribute(String attribute) {
            this.attribute = attribute;
        }

    }

}