/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate51;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate52;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate53;
import org.hibernate.annotations.ForeignKey;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Tuple;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DereferenceManyToOneAssociationInJoinedInheritanceHierarchyTest extends AbstractCoreTest {

    @Override
    protected Properties applyProperties(Properties properties) {
        // Hibernate 5.3+ broke queries on JOINED inheritance hierarchies.
        properties.put("hibernate.query.omit_join_of_superclass_tables", "false");
        return super.applyProperties(properties);
    }

    @Before
    public void setUp() {
        SubA a_1 = new SubA(1L);
        SubA a_2 = new SubA(2L);
        SubA a_3 = new SubA(3L);
        SubA a_14 = em.getReference(SubA.class, 10L);
        SubB b_4 = new SubB(4L, null);
        SubB b_5 = new SubB(5L, a_3);
        SubB b_6 = new SubB(6L, b_4);
        SubB b_7 = new SubB(7L, a_14);

        em.merge(a_1);
        em.merge(a_2);
        em.merge(a_3);
        em.merge(b_4);
        em.merge(b_5);
        em.merge(b_6);
        em.merge(b_7);
    }

    @Test
    @Category({
            // Although unused, this test dependent on the availability of group join support introduced in Hibernate 5.2.8
            NoHibernate42.class, NoHibernate43.class, NoHibernate51.class
    })
    public void testRootTypeJoinWithGroupJoins() {
        Assume.assumeTrue(supportsTableGroupJoins());

        CriteriaBuilder<Tuple> criteriaBuilder = cbf.create(em, Tuple.class)
                .from(SubB.class, "subB_0")
                .leftJoinOn(Super.class, "subA_0").on("subA_0.id").eqExpression("subB_0.parent.id").end()
                .select("subB_0")
                .select("subA_0.id")
                .orderByAsc("subB_0.id")
                .orderByAsc("subA_0.id");
        String queryString = criteriaBuilder
                .getQueryString();

        assertFalse("Query contains unexpected EXIST clause: " + queryString,
                queryString.toUpperCase().contains("EXIST"));

        List<Tuple> resultList = criteriaBuilder.getResultList();

        assertEquals("Rows omitted despite optional association should have rendered a left join", 4, resultList.size());

        assertTrue(resultList.get(0).get(0) instanceof SubB);
        assertTrue(resultList.get(1).get(0) instanceof SubB);
        assertTrue(resultList.get(2).get(0) instanceof SubB);
        assertTrue(resultList.get(3).get(0) instanceof SubB);

        assertEquals((Long) 4L , resultList.get(0).get(0, SubB.class).id);
        assertEquals((Long) 5L , resultList.get(1).get(0, SubB.class).id);
        assertEquals((Long) 6L , resultList.get(2).get(0, SubB.class).id);
        assertEquals((Long) 7L , resultList.get(3).get(0, SubB.class).id);


        assertNull(resultList.get(0).get(1, Long.class));
        assertEquals((Long) 3L , resultList.get(1).get(1, Long.class));
        assertEquals((Long) 4L , resultList.get(2).get(1, Long.class));
        assertNull("Missing entry in foreign table should not be returned", resultList.get(3).get(1, Long.class));
    }

    @Test
    @Category({
            NoHibernate50.class, NoHibernate43.class, NoHibernate42.class, // Entity join required for fallback
            NoHibernate52.class, NoHibernate53.class // Optimize with group joins instead
    })
    public void testRootTypeJoinWithoutGroupJoins() {
        Assume.assumeFalse(supportsTableGroupJoins());

        /*
         * Technically an entity join does not have to be rendered here if the dereferenced property comes from the
         * subclass table span from the entity in the FROM clause (for which JOINS can always be added to the query).
         * However, without table group join support, the property cannot be dereferenced in the JOIN clause. For
         * the isForeign check to be consistent, it has to return false, causing the entity join subquery to be rendered
         * also in the FROM clause case.
         *
         * We may be able to optimize this for Hibernate versions that miss table group joins (<5.2.8).
         */
        Assume.assumeTrue(jpaProvider.supportsEntityJoin());

        CriteriaBuilder<Tuple> criteriaBuilder = cbf.create(em, Tuple.class)
                .from(SubB.class, "subB_0")
                .leftJoinOn(Super.class, "subA_0").on("subA_0.id").eqExpression("subB_0.parent.id").end()
                .select("subB_0")
                .select("subA_0.id")
                .orderByAsc("subB_0.id")
                .orderByAsc("subA_0.id");
        String queryString = criteriaBuilder
                .getQueryString();

        assertEquals("SELECT subB_0, subA_0.id " +
                "FROM SubB subB_0 " +
                "LEFT JOIN subB_0.parent parent_1 " +
                "LEFT JOIN Super subA_0 ON (subA_0.id = parent_1.id) " +
                "ORDER BY subB_0.id ASC, subA_0.id ASC", queryString);

        List<Tuple> resultList = criteriaBuilder.getResultList();

        assertEquals("Rows omitted despite optional association should have rendered a left join", 4, resultList.size());

        assertTrue(resultList.get(0).get(0) instanceof SubB);
        assertTrue(resultList.get(1).get(0) instanceof SubB);
        assertTrue(resultList.get(2).get(0) instanceof SubB);
        assertTrue(resultList.get(3).get(0) instanceof SubB);

        assertEquals((Long) 4L , resultList.get(0).get(0, SubB.class).id);
        assertEquals((Long) 5L , resultList.get(1).get(0, SubB.class).id);
        assertEquals((Long) 6L , resultList.get(2).get(0, SubB.class).id);
        assertEquals((Long) 7L , resultList.get(3).get(0, SubB.class).id);


        assertNull(resultList.get(0).get(1, Long.class));
        assertEquals((Long) 3L , resultList.get(1).get(1, Long.class));
        assertEquals((Long) 4L , resultList.get(2).get(1, Long.class));
        assertNull("Missing entry in foreign table should not be returned", resultList.get(3).get(1, Long.class));
    }

    @Test
    @Category({
            // This test requires group joins introduced in Hibernate 5.2.8
            NoHibernate42.class, NoHibernate43.class, NoHibernate51.class
    })
    public void testSubTypeJoinWithTableGroupJoins() {
        Assume.assumeTrue(supportsTableGroupJoins());

        CriteriaBuilder<Tuple> criteriaBuilder = cbf.create(em, Tuple.class)
                .from(SubB.class, "subB_0")
                .leftJoinOn(SubA.class, "subA_0").on("subA_0.id").eqExpression("subB_0.parent.id").end()
                .select("subB_0")
                .select("subA_0.id")
                .orderByAsc("subB_0.id")
                .orderByAsc("subA_0.id");

        String queryString = criteriaBuilder
                .getQueryString();

        assertFalse("Query contains unexpected EXIST clause: " + queryString,
                queryString.toUpperCase().contains("EXIST"));

        List<Tuple> resultList = criteriaBuilder.getResultList();

        assertEquals("Rows omitted despite optional association should have rendered a left join", 4, resultList.size());

        assertTrue(resultList.get(0).get(0) instanceof SubB);
        assertTrue(resultList.get(1).get(0) instanceof SubB);
        assertTrue(resultList.get(2).get(0) instanceof SubB);
        assertTrue(resultList.get(3).get(0) instanceof SubB);

        assertEquals((Long) 4L , resultList.get(0).get(0, SubB.class).id);
        assertEquals((Long) 5L , resultList.get(1).get(0, SubB.class).id);
        assertEquals((Long) 6L , resultList.get(2).get(0, SubB.class).id);
        assertEquals((Long) 7L , resultList.get(3).get(0, SubB.class).id);

        assertNull(resultList.get(0).get(1, Long.class));
        assertEquals((Long) 3L , resultList.get(1).get(1, Long.class));
        assertNull("Another subtype than queried for was returned", resultList.get(2).get(1));
        assertNull("Missing entry in foreign table should not be returned", resultList.get(3).get(1, Long.class));
    }

    @Test
    @Category({
            NoHibernate50.class, NoHibernate43.class, NoHibernate42.class, // Entity join required for fallback
            NoHibernate52.class, NoHibernate53.class // Optimize with group joins instead
    })
    public void testSubTypeJoinWithoutTableGroupJoins() {
        Assume.assumeFalse(supportsTableGroupJoins());
        Assume.assumeTrue(jpaProvider.supportsEntityJoin());

        CriteriaBuilder<Tuple> criteriaBuilder = cbf.create(em, Tuple.class)
                .from(SubB.class, "subB_0")
                .leftJoinOn(SubA.class, "subA_0").on("subA_0.id").eqExpression("subB_0.parent.id").end()
                .select("subB_0")
                .select("subA_0.id")
                .orderByAsc("subB_0.id")
                .orderByAsc("subA_0.id");

        String queryString = criteriaBuilder
                .getQueryString();

        assertEquals("SELECT subB_0, subA_0.id " +
                "FROM SubB subB_0 " +
                "LEFT JOIN subB_0.parent parent_1 " +
                "LEFT JOIN SubA subA_0 ON (subA_0.id = parent_1.id) " +
                "ORDER BY subB_0.id ASC, subA_0.id ASC", queryString);

        List<Tuple> resultList = criteriaBuilder.getResultList();

        assertEquals("Rows omitted despite optional association should have rendered a left join", 4, resultList.size());

        assertTrue(resultList.get(0).get(0) instanceof SubB);
        assertTrue(resultList.get(1).get(0) instanceof SubB);
        assertTrue(resultList.get(2).get(0) instanceof SubB);
        assertTrue(resultList.get(3).get(0) instanceof SubB);

        assertEquals((Long) 4L , resultList.get(0).get(0, SubB.class).id);
        assertEquals((Long) 5L , resultList.get(1).get(0, SubB.class).id);
        assertEquals((Long) 6L , resultList.get(2).get(0, SubB.class).id);
        assertEquals((Long) 7L , resultList.get(3).get(0, SubB.class).id);

        assertNull(resultList.get(0).get(1, Long.class));
        assertEquals((Long) 3L , resultList.get(1).get(1, Long.class));
        assertNull("Another subtype than queried for was returned", resultList.get(2).get(1));
        assertNull("Missing entry in foreign table should not be returned", resultList.get(3).get(1, Long.class));
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] { Super.class, SubA.class, SubB.class };
    }

    @Entity(name = "Super")
    @Inheritance(strategy = InheritanceType.JOINED)
    public static class Super<SubType extends Super> {

        @Id
        @Column
        Long id;

        @ForeignKey(name = "none")
        @ManyToOne(targetEntity = Super.class, fetch = FetchType.LAZY)
        SubType parent;

    }

    @Entity(name = "SubA")
    public static class SubA extends Super {

        SubA() {}

        SubA(Long id) {
            this.id = id;
        }

    }

    @Entity(name = "SubB")
    public static class SubB extends Super<SubA> {

        SubB() {}

        SubB(Long id, Super parent) {
            this.id = id;
            ((Super) this).parent = parent;
        }

    }

}
