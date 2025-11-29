/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.hibernate;

import com.blazebit.persistence.CTE;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.hibernate.Session;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.util.List;

import static org.hibernate.Hibernate.initialize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.2.1
 */
public class CTESubselectFilteringTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] { Node.class, NodeCTE.class };
    }

    @Before
    public void setUp() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Node parentA = new Node();
                parentA.name = "parentA";
                parentA.filteringValue = true;
                em.persist(parentA);

                Node parentB = new Node();
                parentB.name = "parentB";
                parentB.filteringValue = true;
                em.persist(parentB);

                Node child = new Node();
                child.name = "child";
                child.filteringValue = true;
                child.parent = parentA;
                em.persist(child);

                Node child2 = new Node();
                child2.name = "child2";
                child2.filteringValue = true;
                child2.parent = parentA;
                em.persist(child2);

                Node child3 = new Node();
                child3.name = "child3";
                child3.filteringValue = false;
                child3.parent = parentA;
                em.persist(child3);

                Node child4 = new Node();
                child4.name = "child4";
                child4.filteringValue = true;
                child4.parent = child;
                em.persist(child4);

                Node child5 = new Node();
                child5.name = "child5";
                child5.filteringValue = false;
                child5.parent = child;
                em.persist(child5);

                em.flush();
                em.clear();
            }
        });
    }

    @Test
    @Category({ NoMySQL.class, NoOracle.class})
    public void testFilteringForCTESubselectLoader() {
        em.unwrap(Session.class).enableFilter("NodeFilter")
                .setParameter("mySuperValue", true);

        List<Node> resultList = cbf.create(em, Node.class)
                .with(NodeCTE.class, false)
                .from(Node.class)
                    .where("name").eq("bogus")
                    .bind("id").select("name")
                .end()
                .from(Node.class)
                .whereOr()
                    // CTE is empty here under H2 and hence is not actually filtering the results,
                    // I am addressing it here as future optimization might remove unused CTE's from the
                    // produced SQL
                    .where("name").in().from(NodeCTE.class).select("id").end()
                    // Its mandatory to produce >1 results, as otherwise SELECT is used instead of SUBSELECT
                    .where("name").eq("parentA")
                    .where("name").eq("parentB")
                .endOr()
                .orderByAsc("id")
                .getResultList();

        assertFalse(resultList.isEmpty());

        Node parent = resultList.get(0);

        assertEquals("parentA", parent.name);

        // First subselect
        initialize(parent.children);
        assertEquals(2, parent.children.size());

        // Second, nested subselect
        Node child = parent.children.get(0);
        initialize(child.children);
        assertEquals(1, child.children.size());
    }

    @CTE
    @Entity(name = "NodeCte")
    public static class NodeCTE {

        @Id
        private String id;

    }

    @Entity(name = "Node")
    @FilterDef(name = "NodeFilter", defaultCondition = "filtering_value = :mySuperValue", parameters = {
            @ParamDef(name = "mySuperValue", type = Boolean.class)
    })
    public static class Node {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String name;

        @Column(name = "filtering_value")
        private boolean filteringValue;

        @ManyToOne
        private Node parent;

        @OrderBy("id")
        @Fetch(FetchMode.SUBSELECT)
        @Filter(name = "NodeFilter")
        @OneToMany(mappedBy = "parent")
        private List<Node> children;

    }

}
