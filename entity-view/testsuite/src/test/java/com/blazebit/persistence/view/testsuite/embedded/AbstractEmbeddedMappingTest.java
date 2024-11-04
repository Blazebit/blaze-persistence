/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.embedded;

import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import org.junit.Before;

import javax.persistence.EntityManager;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AbstractEmbeddedMappingTest extends AbstractEntityViewTest {

    protected Document doc1;
    protected Document doc2;

    @Before
    public void setUp() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Person p = new Person("pers");
                doc1 = new Document("doc1", p);
                doc2 = new Document("doc2", p);

                em.persist(p);
                em.persist(doc1);
                em.persist(doc2);
            }
        });
    }
}
