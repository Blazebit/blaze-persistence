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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;

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
