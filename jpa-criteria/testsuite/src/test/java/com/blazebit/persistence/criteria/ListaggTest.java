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

package com.blazebit.persistence.criteria;

import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDB2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMSSQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Document_;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.entity.Version_;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.JoinType;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.6.4
 */
public class ListaggTest extends AbstractCoreTest {

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Person p = new Person("Pers1");
                p.setAge(20L);
                em.persist(p);

                Version v1 = new Version();
                Document doc1 = new Document("Doc1", p, v1);
                em.persist(doc1);
                em.persist(v1);

                Version v2 = new Version();
                v2.setUrl("b");
                Document doc2 = new Document("Doc1", p, v2);
                em.persist(doc2);
                em.persist(v2);

                Version v3 = new Version();
                v3.setUrl("a");
                Document doc3 = new Document("Doc2", p, v3);
                em.persist(doc3);
                em.persist(v3);
            }
        });
    }

    // NOTE: DB2 crashes when executing this test
    @Test
    @Category({ NoDB2.class })
    public void testSimpleWithDefault() {
        BlazeCriteriaBuilder cb = BlazeCriteria.get(cbf);
        BlazeCriteriaQuery<Tuple> tupleQuery = cb.createTupleQuery();
        BlazeRoot<Document> doc = tupleQuery.from(Document.class, "doc");
        tupleQuery.multiselect(
                cb.listagg(doc.get(Document_.name), cb.literal(","))
                        .withinGroup(cb.asc(doc.get(Document_.name)))
        );
        tupleQuery.groupBy(doc.get(Document_.owner));

        Tuple actual = tupleQuery.createCriteriaBuilder(em).getResultList().get(0);

        assertEquals("Doc1,Doc1,Doc2", actual.get(0));
    }

    // NOTE: DB2 crashes when executing this test
    @Test
    @Category({ NoDB2.class })
    public void testSimpleWithSeparator() {
        BlazeCriteriaBuilder cb = BlazeCriteria.get(cbf);
        BlazeCriteriaQuery<Tuple> tupleQuery = cb.createTupleQuery();
        BlazeRoot<Document> doc = tupleQuery.from(Document.class, "doc");
        tupleQuery.multiselect(
                cb.listagg(doc.get(Document_.name), cb.literal(", "))
                        .withinGroup(cb.asc(doc.get(Document_.name)))
        );
        tupleQuery.groupBy(doc.get(Document_.owner));

        Tuple actual = tupleQuery.createCriteriaBuilder(em).getResultList().get(0);

        assertEquals("Doc1, Doc1, Doc2", actual.get(0));
    }

    // NOTE: DB2 apparently does not support distinct?!
    // NOTE: DB2 crashes when executing this test
    // NOTE: Oracle apparently does not support distinct?!
    // NOTE: SQL Server apparently does not support distinct?!
    // Documentation states it does https://www-01.ibm.com/support/knowledgecenter/SS6NHC/com.ibm.swg.im.dashdb.sql.ref.doc/doc/r0058709.html
    // See http://stackoverflow.com/questions/35309065/db2-listagg-with-distinct
    // See http://dba.stackexchange.com/questions/696/eliminate-duplicates-in-listagg-oracle
    @Test
    @Category({ NoDB2.class, NoOracle.class, NoMSSQL.class })
    public void testDistinct() {
        BlazeCriteriaBuilder cb = BlazeCriteria.get(cbf);
        BlazeCriteriaQuery<Tuple> tupleQuery = cb.createTupleQuery();
        BlazeRoot<Document> doc = tupleQuery.from(Document.class, "doc");
        tupleQuery.multiselect(
                cb.listaggDistinct(doc.get(Document_.name), cb.literal(", "))
                        .withinGroup(cb.asc(doc.get(Document_.name)))
        );
        tupleQuery.groupBy(doc.get(Document_.owner));

        Tuple actual = tupleQuery.createCriteriaBuilder(em).getResultList().get(0);

        assertEquals("Doc1, Doc2", actual.get(0));
    }

    // NOTE: DB2 crashes when executing this test
    @Test
    @Category({ NoDB2.class })
    public void testDescNullsLast() {
        BlazeCriteriaBuilder cb = BlazeCriteria.get(cbf);
        BlazeCriteriaQuery<Tuple> tupleQuery = cb.createTupleQuery();
        BlazeRoot<Document> doc = tupleQuery.from(Document.class, "doc");
        BlazeSetJoin<Document, Version> versions = doc.join(Document_.versions, JoinType.LEFT);
        tupleQuery.multiselect(
                cb.listagg(versions.get(Version_.url), cb.literal(", "))
                        .withinGroup(cb.desc(versions.get(Version_.url), false))
        );
        tupleQuery.groupBy(doc.get(Document_.owner));

        Tuple actual = tupleQuery.createCriteriaBuilder(em).getResultList().get(0);

        assertEquals("b, a", actual.get(0));
    }
}
