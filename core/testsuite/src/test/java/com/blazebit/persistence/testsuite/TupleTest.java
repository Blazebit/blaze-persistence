/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.2.0
 */
public class TupleTest extends AbstractCoreTest {

    // from issue #490
    @Test
    public void testGetTupleElementAlias() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Document d = new Document("D1");

                Person p1 = new Person("Joe");
                Person p2 = new Person("Fred");
                d.setOwner(p1);
                d.getPartners().add(p1);
                d.getPartners().add(p2);

                em.persist(p1);
                em.persist(p2);
                em.persist(d);
            }
        });

        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class).from(Document.class, "d").select("d.id", "theId");

        Tuple result = criteria.getResultList().get(0);
        String alias = result.getElements().get(0).getAlias();
        assertEquals("theId", alias);
        assertEquals(result.get(0), result.get("theId"));
    }

}
