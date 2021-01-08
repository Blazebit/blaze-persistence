/*
 * Copyright 2014 - 2021 Blazebit.
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
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQLOld;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Tuple;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


/**
 * Test that tests full join behaviour under Hibernate.
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
@Category({NoH2.class, NoMySQL.class, NoMySQLOld.class, NoHibernate.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class})
public class FullJoinTest extends AbstractCoreTest {

    @Before
    public void setUp() {
        Assume.assumeTrue(jpaProvider.supportsEntityJoin());

        Association association_1 = new Association();
        association_1.name = "Peter";
        em.persist(association_1);


        Association association_2 = new Association();
        association_2.name = "Peter";
        em.persist(association_2);


        Association association_3 = new Association();
        association_3.name = "Peter";
        em.persist(association_3);

        A a_1 = new A(1L, 10L, association_1);
        A a_2 = new A(2L, 10L, association_1);
        A a_3 = new A(3L, 10L, association_1);

        B b_1 = new B(1L, 10L, association_2);
        B b_2 = new B(2L, 10L, association_2);
        B b_4 = new B(4L, 10L, association_2);

        em.merge(a_1);
        em.merge(a_2);
        em.merge(a_3);
        em.merge(b_1);
        em.merge(b_2);
        em.merge(b_4);
    }

    @Test
    public void testFullOuterJoin() {
        CriteriaBuilder<Tuple> end = cbf.create(em, Tuple.class)
                .from(A.class, "a")
                .joinOn(B.class, "b", JoinType.FULL).on("a.id").eqExpression("b.id").end()
                .orderByAsc("a.id")
                .orderByAsc("b.id")
                .select("COALESCE(a.id, b.id)")
                .select("a.a")
                .select("b.b")
                .select("a.a + b.b");

        String queryString = end.getQueryString();

        List<Tuple> resultList = end.getResultList();
        assertEquals(4L, resultList.size());

        assertEquals(1L, resultList.get(0).get(0));
        assertEquals(10L, resultList.get(0).get(1));
        assertEquals(10L, resultList.get(0).get(2));
        assertEquals(20L, resultList.get(0).get(3));

        assertEquals(2L, resultList.get(1).get(0));
        assertEquals(10L, resultList.get(1).get(1));
        assertEquals(10L, resultList.get(1).get(2));
        assertEquals(20L, resultList.get(1).get(3));

        assertEquals(3L, resultList.get(2).get(0));
        assertEquals(10L, resultList.get(2).get(1));
        assertNull(resultList.get(2).get(2));
        assertNull(resultList.get(2).get(3));

        assertEquals(4L, resultList.get(3).get(0));
        assertNull(resultList.get(3).get(1));
        assertEquals(10L, resultList.get(3).get(2));
        assertNull(resultList.get(3).get(3));
    }

    @Test
    public void testFullOuterJoinWithRelationJoin() {
        CriteriaBuilder<Tuple> end = cbf.create(em, Tuple.class)
                .from(A.class, "a")
                .joinOn(B.class, "b", JoinType.FULL).on("a.id").eqExpression("b.id").end()
                .orderByAsc("a.id")
                .orderByAsc("b.id")
                .select("COALESCE(a.id, b.id)")
                .select("a.a")
                .select("b.b")
                .select("a.a + b.b")
                .select("a.association.name")
                .select("b.association.name");

        String queryString = end.getQueryString();

        List<Tuple> resultList = end.getResultList();
        assertEquals(4L, resultList.size());

        assertEquals(1L, resultList.get(0).get(0));
        assertEquals(10L, resultList.get(0).get(1));
        assertEquals(10L, resultList.get(0).get(2));
        assertEquals(20L, resultList.get(0).get(3));

        assertEquals(2L, resultList.get(1).get(0));
        assertEquals(10L, resultList.get(1).get(1));
        assertEquals(10L, resultList.get(1).get(2));
        assertEquals(20L, resultList.get(1).get(3));

        assertEquals(3L, resultList.get(2).get(0));
        assertEquals(10L, resultList.get(2).get(1));
        assertNull(resultList.get(2).get(2));
        assertNull(resultList.get(2).get(3));

        assertEquals(4L, resultList.get(3).get(0));
        assertNull(resultList.get(3).get(1));
        assertEquals(10L, resultList.get(3).get(2));
        assertNull(resultList.get(3).get(3));
    }

    @Test
    public void testFullOuterJoinWithRelationJoinEntity() {
        CriteriaBuilder<Tuple> end = cbf.create(em, Tuple.class)
                .from(A.class, "a")
                .joinOn(B.class, "b", JoinType.FULL).on("a.id").eqExpression("b.id").end()
                .fetch("a.association")
                .fetch("b.association")
                .orderByAsc("a.id")
                .orderByAsc("b.id")
                .select("COALESCE(a.id, b.id)")
                .select("a.a")
                .select("b.b")
                .select("a.a + b.b")
                .select("a")
                .select("b");

        String queryString = end.getQueryString();

        List<Tuple> resultList = end.getResultList();
        assertEquals(4L, resultList.size());

        assertEquals(1L, resultList.get(0).get(0));
        assertEquals(10L, resultList.get(0).get(1));
        assertEquals(10L, resultList.get(0).get(2));
        assertEquals(20L, resultList.get(0).get(3));

        assertEquals(2L, resultList.get(1).get(0));
        assertEquals(10L, resultList.get(1).get(1));
        assertEquals(10L, resultList.get(1).get(2));
        assertEquals(20L, resultList.get(1).get(3));

        assertEquals(3L, resultList.get(2).get(0));
        assertEquals(10L, resultList.get(2).get(1));
        assertNull(resultList.get(2).get(2));
        assertNull(resultList.get(2).get(3));

        assertEquals(4L, resultList.get(3).get(0));
        assertNull(resultList.get(3).get(1));
        assertEquals(10L, resultList.get(3).get(2));
        assertNull(resultList.get(3).get(3));
    }

    @Test(expected = IllegalStateException.class)
    public void testFullJoinPagination() {
        PaginatedCriteriaBuilder<Tuple> end = cbf.create(em, Tuple.class)
                .from(A.class, "a")
                .joinOn(B.class, "b", JoinType.FULL).on("a.id").eqExpression("b.id").end()
                .orderByAsc("COALESCE(a.id, b.id)")
                .select("COALESCE(a.id, b.id)")
                .select("a.a")
                .select("b.b")
                .select("a.a + b.b")
                .select("a")
                .select("b")
                .page(0, 2);

        end.getResultList();
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] { A.class, B.class, Association.class };
    }

    @Entity
    public static class A {

        @Id
        Long id;

        Long a;

        @ManyToOne(optional = false)
        Association association;

        public A() {}

        public A(Long id, Long a, Association association) {
            this.id = id;
            this.a = a;
            this.association = association;
        }
    }

    @Entity
    public static class B {

        @Id
        Long id;

        Long b;

        @ManyToOne(optional = false)
        Association association;


        public B() {}

        public B(Long id, Long b, Association association) {
            this.id = id;
            this.b = b;
            this.association = association;
        }
    }

    @Entity
    public static class Association {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        Long id;

        String name;

    }
}
