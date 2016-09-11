package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CTE;
import com.blazebit.persistence.CriteriaBuilder;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EntityTransaction;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jan-Willem Gmelig Meyling
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
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            RecursiveEntity root1 = new RecursiveEntity("root1");
            RecursiveEntity child1_1 = new RecursiveEntity("child1_1", root1);
            RecursiveEntity child1_2 = new RecursiveEntity("child1_2", root1);

            RecursiveEntity child1_1_1 = new RecursiveEntity("child1_1_1", child1_1);
            RecursiveEntity child1_2_1 = new RecursiveEntity("child1_2_1", child1_2);

            em.persist(root1);
            em.persist(child1_1);
            em.persist(child1_2);
            em.persist(child1_1_1);
            em.persist(child1_2_1);

            em.flush();
            // Clear is important here, because otherwise we obtain the cached entity,
            // for which children=null, which is not properly initialized by Hibernate#initialize(Object)
            em.clear();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
    }

    @Test
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
            .fetch("parent");

        List<RecursiveEntityCte> result = cb.getResultList();
        RecursiveEntity firstParent = result.get(0).getParent();
        Hibernate.initialize(firstParent.getChildren());
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

    }

    @CTE
    @Entity
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
