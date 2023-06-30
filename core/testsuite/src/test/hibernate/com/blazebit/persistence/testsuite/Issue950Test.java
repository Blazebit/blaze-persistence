/*
 * Copyright 2014 - 2023 Blazebit.
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

import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate51;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate52;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate53;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Tuple;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.3.0
 */
public class Issue950Test extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{ A.class, B.class, C.class, D.class, Association.class };
    }

    @MappedSuperclass
    public static class BaseEntity {

        @Id
        @Column(name = "key_id")
        Long key;

        @Column(name = "val")
        String value;

        @ManyToOne(optional = false)
        Association association;

        public BaseEntity() {
        }

        public BaseEntity(Long key, String value, Association association) {
            this.key = key;
            this.value = value;
            this.association = association;
        }
    }

    @Entity(name = "A")
    @Table(name = "a")
    public static class A extends BaseEntity {

        @OneToOne
        @PrimaryKeyJoinColumn(columnDefinition = "association_key", referencedColumnName = "key_id")
        private C cAssociationByKey;

        public A() {
        }

        public A(Long key, String value, Association association) {
            super(key, value, association);
        }
    }


    @Entity(name = "B")
    @Table(name = "b")
    public static class B extends BaseEntity {
        public B() {
        }

        public B(Long key, String value, Association association) {
            super(key, value, association);
        }
    }

    @Entity(name = "C")
    @Table(name = "c")
    public static class C extends BaseEntity {
        public C() {
        }

        public C(Long key, String value, Association association) {
            super(key, value, association);
        }
    }

    @Entity(name = "D")
    @Table(name = "d")
    public static class D extends BaseEntity {
        public D() {
        }

        public D(Long key, String value, Association association) {
            super(key, value, association);
        }

    }

    @Entity(name = "Association")
    @Table(name = "association")
    public static class Association {

        @Id
        @Column(name = "key_id")
        private Long key;

        @Column(name = "val")
        private String value;

        public Association() {
        }

        public Association(Long key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    @Before
    public void setUp() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Association association = new Association(1l, "association");
                em.merge(association);

                em.merge(new A(1L, "a", association));
                em.merge(new A(2L, "b", association));
                em.merge(new A(3L, "c", association));

                em.merge(new B(1L, "d", association));
                em.merge(new B(2L, "e", association));
                em.merge(new B(3L, "f", association));

                em.merge(new C(1L, "g", association));
                em.merge(new C(2L, "h", association));
                em.merge(new C(4L, "j", association));

                em.merge(new D(1L, "k", association));
                em.merge(new D(2L, "l", association));
                em.merge(new D(4L, "m", association));
            }
        });
    }

    // NOTE: Requires Entity joins
    @Test
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class })
    public void testJoinOrderWithRightJoin() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                List<Tuple> resultList = cbf.create(em, Tuple.class)
                        .from(A.class, "a")
                        .innerJoinOn(B.class, "b").on("a.key").eqExpression("b.key").end()
                        .rightJoinOn(C.class, "c").on("a.key").eqExpression("c.key").end()
                        .innerJoinOn(D.class, "d").on("d.key").eqExpression("c.key").end()
                        .select("COALESCE(a.key, b.key, c.key, d.key)", "key")
                        .select("a.value", "aValue")
                        .select("b.value", "bValue")
                        .select("c.value", "cValue")
                        .select("d.value", "dValue")
                        .orderByAsc("COALESCE(a.key, b.key, c.key, d.key)")
                        .getResultList();

                assertEquals(3, resultList.size());

                assertEquals("a", resultList.get(0).get(1));
                assertEquals("d", resultList.get(0).get(2));
                assertEquals("g", resultList.get(0).get(3));
                assertEquals("k", resultList.get(0).get(4));

                assertEquals("b", resultList.get(1).get(1));
                assertEquals("e", resultList.get(1).get(2));
                assertEquals("h", resultList.get(1).get(3));
                assertEquals("l", resultList.get(1).get(4));

                assertNull(resultList.get(2).get(1));
                assertNull(resultList.get(2).get(2));
                assertEquals("j", resultList.get(2).get(3));
                assertEquals("m", resultList.get(2).get(4));
            }
        });
    }

    // NOTE: Hibernate currently doesn't fully respect the join order, but just renders association joins first
    @Test
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoHibernate52.class, NoHibernate53.class, NoHibernate.class })
    public void testJoinOrderWithRightNormalJoin() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                List<Tuple> resultList = cbf.create(em, Tuple.class)
                        .from(A.class, "a")
                        .innerJoinOn(B.class, "b").on("a.key").eqExpression("b.key").end()
                        .rightJoin("a.cAssociationByKey", "c")
                        .innerJoinOn(D.class, "d").on("d.key").eqExpression("c.key").end()
                        .select("COALESCE(a.key, b.key, c.key, d.key)", "key")
                        .select("a.value", "aValue")
                        .select("b.value", "bValue")
                        .select("c.value", "cValue")
                        .select("d.value", "dValue")
                        .orderByAsc("COALESCE(a.key, b.key, c.key, d.key)")
                        .getResultList();

                assertEquals(3, resultList.size());

                assertEquals("a", resultList.get(0).get(1));
                assertEquals("d", resultList.get(0).get(2));
                assertEquals("g", resultList.get(0).get(3));
                assertEquals("k", resultList.get(0).get(4));

                assertEquals("b", resultList.get(1).get(1));
                assertEquals("e", resultList.get(1).get(2));
                assertEquals("h", resultList.get(1).get(3));
                assertEquals("l", resultList.get(1).get(4));

                assertNull(resultList.get(2).get(1));
                assertNull(resultList.get(2).get(2));
                assertEquals("j", resultList.get(2).get(3));
                assertEquals("m", resultList.get(2).get(4));
            }
        });
    }

    // NOTE: Requires Entity joins
    @Test
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class })
    public void testJoinOrderWithRightJoinWithIdDereference() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                List<Tuple> resultList = cbf.create(em, Tuple.class)
                        .from(A.class, "a")
                        .innerJoinOn(B.class, "b")
                            .on("a.key").eqExpression("b.key")
                            .on("a.association.key").eqExpression("b.association.key")
                            .end()
                        .rightJoinOn(C.class, "c")
                            .on("a.key").eqExpression("c.key")
                            .on("a.association.key").eqExpression("c.association.key")
                            .end()
                        .innerJoinOn(D.class, "d")
                            .on("d.key").eqExpression("c.key")
                            .on("d.association.key").eqExpression("c.association.key")
                            .end()
                        .select("COALESCE(a.key, b.key, c.key, d.key)", "key")
                        .select("a.value", "aValue")
                        .select("b.value", "bValue")
                        .select("c.value", "cValue")
                        .select("d.value", "dValue")
                        .orderByAsc("COALESCE(a.key, b.key, c.key, d.key)")
                        .getResultList();

                assertEquals(3, resultList.size());

                assertEquals("a", resultList.get(0).get(1));
                assertEquals("d", resultList.get(0).get(2));
                assertEquals("g", resultList.get(0).get(3));
                assertEquals("k", resultList.get(0).get(4));

                assertEquals("b", resultList.get(1).get(1));
                assertEquals("e", resultList.get(1).get(2));
                assertEquals("h", resultList.get(1).get(3));
                assertEquals("l", resultList.get(1).get(4));

                assertNull(resultList.get(2).get(1));
                assertNull(resultList.get(2).get(2));
                assertEquals("j", resultList.get(2).get(3));
                assertEquals("m", resultList.get(2).get(4));
            }
        });
    }

    // NOTE: Hibernate currently doesn't fully respect the join order, but just renders association joins first
    @Test
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoHibernate52.class, NoHibernate53.class, NoHibernate.class })
    public void testJoinOrderWithRightNormalJoinWithIdDereference() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                List<Tuple> resultList = cbf.create(em, Tuple.class)
                        .from(A.class, "a")
                        .innerJoinOn(B.class, "b")
                            .on("a.key").eqExpression("b.key")
                            .on("a.association.key").eqExpression("b.association.key")
                            .end()
                        .rightJoinOn("a.cAssociationByKey", "c")
                            .on("a.key").eqExpression("c.key")
                            .on("a.association.key").eqExpression("c.association.key")
                            .end()
                        .innerJoinOn(D.class, "d")
                            .on("d.key").eqExpression("c.key")
                            .on("d.association.key").eqExpression("c.association.key")
                            .end()
                        .select("COALESCE(a.key, b.key, c.key, d.key)", "key")
                        .select("a.value", "aValue")
                        .select("b.value", "bValue")
                        .select("c.value", "cValue")
                        .select("d.value", "dValue")
                        .orderByAsc("COALESCE(a.key, b.key, c.key, d.key)")
                        .getResultList();

                assertEquals(3, resultList.size());

                assertEquals("a", resultList.get(0).get(1));
                assertEquals("d", resultList.get(0).get(2));
                assertEquals("g", resultList.get(0).get(3));
                assertEquals("k", resultList.get(0).get(4));

                assertEquals("b", resultList.get(1).get(1));
                assertEquals("e", resultList.get(1).get(2));
                assertEquals("h", resultList.get(1).get(3));
                assertEquals("l", resultList.get(1).get(4));

                assertNull(resultList.get(2).get(1));
                assertNull(resultList.get(2).get(2));
                assertEquals("j", resultList.get(2).get(3));
                assertEquals("m", resultList.get(2).get(4));
            }
        });
    }

    // NOTE: Requires Entity joins
    @Test
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class })
    public void testJoinOrderWithRightJoinWithInnerImplicitJoins() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                List<Tuple> resultList = cbf.create(em, Tuple.class)
                        .from(A.class, "a")
                        .innerJoinOn(B.class, "b")
                            .on("a.key").eqExpression("b.key")
                            .on("a.association.value").eqExpression("b.association.value")
                            .end()
                        .rightJoinOn(C.class, "c")
                            .on("a.key").eqExpression("c.key")
                            .on("a.association.value").eqExpression("c.association.value")
                            .end()
                        .innerJoinOn(D.class, "d")
                            .on("d.key").eqExpression("c.key")
                            .on("d.association.value").eqExpression("c.association.value")
                            .end()
                        .select("COALESCE(a.key, b.key, c.key, d.key)", "key")
                        .select("a.value", "aValue")
                        .select("b.value", "bValue")
                        .select("c.value", "cValue")
                        .select("d.value", "dValue")
                        .orderByAsc("COALESCE(a.key, b.key, c.key, d.key)")
                        .getResultList();

                assertEquals(3, resultList.size());

                assertEquals("a", resultList.get(0).get(1));
                assertEquals("d", resultList.get(0).get(2));
                assertEquals("g", resultList.get(0).get(3));
                assertEquals("k", resultList.get(0).get(4));

                assertEquals("b", resultList.get(1).get(1));
                assertEquals("e", resultList.get(1).get(2));
                assertEquals("h", resultList.get(1).get(3));
                assertEquals("l", resultList.get(1).get(4));

                assertNull(resultList.get(2).get(1));
                assertNull(resultList.get(2).get(2));
                assertEquals("j", resultList.get(2).get(3));
                assertEquals("m", resultList.get(2).get(4));
            }
        });
    }

    // NOTE: Hibernate currently doesn't fully respect the join order, but just renders association joins first
    @Test
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoHibernate52.class, NoHibernate53.class, NoHibernate.class })
    public void testJoinOrderWithRightNormalJoinWithInnerImplicitJoins() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                List<Tuple> resultList = cbf.create(em, Tuple.class)
                        .from(A.class, "a")
                        .innerJoinOn(B.class, "b")
                            .on("a.key").eqExpression("b.key")
                            .on("a.association.value").eqExpression("b.association.value")
                            .end()
                        .rightJoinOn("a.cAssociationByKey", "c")
                            .on("a.key").eqExpression("c.key")
                            .on("a.association.value").eqExpression("c.association.value")
                            .end()
                        .innerJoinOn(D.class, "d")
                            .on("d.key").eqExpression("c.key")
                            .on("d.association.value").eqExpression("c.association.value")
                            .end()
                        .select("COALESCE(a.key, b.key, c.key, d.key)", "key")
                        .select("a.value", "aValue")
                        .select("b.value", "bValue")
                        .select("c.value", "cValue")
                        .select("d.value", "dValue")
                        .orderByAsc("COALESCE(a.key, b.key, c.key, d.key)")
                        .getResultList();

                assertEquals(3, resultList.size());

                assertEquals("a", resultList.get(0).get(1));
                assertEquals("d", resultList.get(0).get(2));
                assertEquals("g", resultList.get(0).get(3));
                assertEquals("k", resultList.get(0).get(4));

                assertEquals("b", resultList.get(1).get(1));
                assertEquals("e", resultList.get(1).get(2));
                assertEquals("h", resultList.get(1).get(3));
                assertEquals("l", resultList.get(1).get(4));

                assertNull(resultList.get(2).get(1));
                assertNull(resultList.get(2).get(2));
                assertEquals("j", resultList.get(2).get(3));
                assertEquals("m", resultList.get(2).get(4));
            }
        });
    }

    // NOTE: Requires Entity joins
    @Test
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class })
    public void testJoinOrderWithRightJoinWithNonOptionalAssociationProjections() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                List<Tuple> resultList = cbf.create(em, Tuple.class)
                        .from(A.class, "a")
                        .innerJoinOn(B.class, "b").on("a.key").eqExpression("b.key").end()
                        .rightJoinOn(C.class, "c").on("a.key").eqExpression("c.key").end()
                        .innerJoinOn(D.class, "d").on("d.key").eqExpression("c.key").end()
                        .select("COALESCE(a.key, b.key, c.key, d.key)", "key")
                        .select("a.value", "aValue")
                        .select("b.value", "bValue")
                        .select("c.value", "cValue")
                        .select("d.value", "dValue")
                        .select("a.association.value", "aAssociationValue")
                        .select("b.association.value", "bAssociationValue")
                        .select("c.association.value", "cAssociationValue")
                        .select("d.association.value", "dAssociationValue")
                        .orderByAsc("COALESCE(a.key, b.key, c.key, d.key)")
                        .getResultList();

                assertEquals(3, resultList.size());

                assertEquals("a", resultList.get(0).get(1));
                assertEquals("d", resultList.get(0).get(2));
                assertEquals("g", resultList.get(0).get(3));
                assertEquals("k", resultList.get(0).get(4));

                assertEquals("b", resultList.get(1).get(1));
                assertEquals("e", resultList.get(1).get(2));
                assertEquals("h", resultList.get(1).get(3));
                assertEquals("l", resultList.get(1).get(4));

                assertNull(resultList.get(2).get(1));
                assertNull(resultList.get(2).get(2));
                assertEquals("j", resultList.get(2).get(3));
                assertEquals("m", resultList.get(2).get(4));
            }
        });
    }

    // NOTE: Hibernate currently doesn't fully respect the join order, but just renders association joins first
    @Test
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoHibernate52.class, NoHibernate53.class, NoHibernate.class })
    public void testJoinOrderWithRightNormalJoinWithNonOptionalAssociationProjections() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                List<Tuple> resultList = cbf.create(em, Tuple.class)
                        .from(A.class, "a")
                        .innerJoinOn(B.class, "b").on("a.key").eqExpression("b.key").end()
                        .rightJoinOn("a.cAssociationByKey", "c").on("a.key").eqExpression("c.key").end()
                        .innerJoinOn(D.class, "d").on("d.key").eqExpression("c.key").end()
                        .select("COALESCE(a.key, b.key, c.key, d.key)", "key")
                        .select("a.value", "aValue")
                        .select("b.value", "bValue")
                        .select("c.value", "cValue")
                        .select("d.value", "dValue")
                        .select("a.association.value", "aAssociationValue")
                        .select("b.association.value", "bAssociationValue")
                        .select("c.association.value", "cAssociationValue")
                        .select("d.association.value", "dAssociationValue")
                        .orderByAsc("COALESCE(a.key, b.key, c.key, d.key)")
                        .getResultList();

                assertEquals(3, resultList.size());

                assertEquals("a", resultList.get(0).get(1));
                assertEquals("d", resultList.get(0).get(2));
                assertEquals("g", resultList.get(0).get(3));
                assertEquals("k", resultList.get(0).get(4));

                assertEquals("b", resultList.get(1).get(1));
                assertEquals("e", resultList.get(1).get(2));
                assertEquals("h", resultList.get(1).get(3));
                assertEquals("l", resultList.get(1).get(4));

                assertNull(resultList.get(2).get(1));
                assertNull(resultList.get(2).get(2));
                assertEquals("j", resultList.get(2).get(3));
                assertEquals("m", resultList.get(2).get(4));
            }
        });
    }
    
}