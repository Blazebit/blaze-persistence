package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CTE;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.hibernate.Session;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

import static org.hibernate.Hibernate.initialize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
                child2.filteringValue = false;
                child2.parent = parentA;
                em.persist(child2);


                Node child3 = new Node();
                child3.name = "child3";
                child3.filteringValue = true;
                child3.parent = child;
                em.persist(child3);

                Node child4 = new Node();
                child4.name = "child4";
                child4.filteringValue = false;
                child4.parent = child;
                em.persist(child4);

                em.flush();
                em.clear();
            }
        });
    }

    @Test
    public void testFilteringForCTESubselectLoader() {
        List<Node> resultList = cbf.create(em, Node.class)
                .with(NodeCTE.class)
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

        assertEquals(parent.name, "parentA");

        em.unwrap(Session.class).enableFilter("NodeFilter")
                .setParameter("mySuperValue", true);

        initialize(parent.children);

        assertEquals(parent.children.size(), 1);
    }

    @CTE
    @Entity(name = "NodeCte")
    public static class NodeCTE {

        @Id
        private String id;

    }

    @Entity(name = "Node")
    @FilterDef(name = "NodeFilter", defaultCondition = "filtering_value = :mySuperValue", parameters = {
            @ParamDef(name = "mySuperValue", type = "boolean")
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

        @Fetch(FetchMode.SUBSELECT)
        @Filter(name = "NodeFilter")
        @OneToMany(mappedBy = "parent")
        private List<Node> children;

    }

}
