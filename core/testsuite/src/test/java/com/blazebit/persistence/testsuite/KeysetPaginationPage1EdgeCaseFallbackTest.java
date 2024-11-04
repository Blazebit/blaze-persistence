/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.entity.Workflow;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class KeysetPaginationPage1EdgeCaseFallbackTest extends AbstractCoreTest {

    private Person o4;

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Document doc1 = new Document("doc1");
                Document doc2 = new Document("doc2");
                Document doc3 = new Document("doc3");
                Document doc4 = new Document("doc4");

                Person o1 = new Person("Karl1");
                Person o2 = new Person("Karl2");
                Person o3 = new Person("Karl3");
                o4 = new Person("Karl4");

                doc1.setOwner(o1);
                doc2.setOwner(o2);
                doc3.setOwner(o3);
                doc4.setOwner(o4);

                em.persist(o1);
                em.persist(o2);
                em.persist(o3);
                em.persist(o4);

                em.persist(doc1);
                em.persist(doc2);
                em.persist(doc3);
                em.persist(doc4);
            }
        });
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
                Document.class,
                Version.class,
                Person.class,
                Workflow.class,
                IntIdEntity.class,
                Document.class
        };
    }

    @Test
    public void keysetPaginationPage1EdgeCaseFallback() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.name").select("d.owner.name");
        crit.orderByDesc("d.owner.name")
                .orderByDesc("d.name")
                .orderByAsc("d.id");
        /* query yields the following order:
         *  - doc4
         *  - doc3
         *  - doc2
         *  - doc1
         */

        PaginatedCriteriaBuilder<Tuple> pcb = crit.page(null, 0, 2);
        PagedList<Tuple> result = pcb.getResultList();

        // scroll forward
        result = crit.page(result.getKeysetPage(), 2, 2).getResultList();

        // prepend element
        em.persist(new Document("doc5", em.getReference(Person.class, o4.getId())));
        em.flush();

        // scroll backwards
        result = crit.page(result.getKeysetPage(), 0, 2).getResultList();

        assertNotNull(result.getKeysetPage());
        assertEquals(2, result.getSize());
        assertEquals("doc5", result.get(0).get(0));
        assertEquals("doc4", result.get(1).get(0));
    }
}
