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
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus4;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
// NOTE: Entity joins are supported since Hibernate 5.1, Datanucleus 5 and latest Eclipselink
@Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class, NoOpenJPA.class})
public class EntityJoinTest extends AbstractCoreTest {

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Document doc1 = new Document("doc1", 2);
                Document doc2 = new Document("doc2", 3);
                Document doc3 = new Document("doc3", 5);

                Person o1 = new Person("pers1", 1);
                Person o2 = new Person("pers2", 4);
                Person o3 = new Person("doc1", 0);

                doc1.setOwner(o1);
                doc2.setOwner(o1);
                doc3.setOwner(o1);

                doc1.getContacts().put(1, o1);
                doc1.getContacts().put(2, o2);

                em.persist(o1);
                em.persist(o2);
                em.persist(o3);

                em.persist(doc1);
                em.persist(doc2);
                em.persist(doc3);
            }
        });
    }

    @Test
    public void testEntityInnerJoin() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class)
                .from(Document.class, "d")
                .innerJoinOn(Person.class, "p")
                    .on("p.age").geExpression("d.age")
                .end()
                .select("d.name").select("p.name")
                .orderByAsc("d.name");
        assertEquals("SELECT d.name, p.name FROM Document d JOIN Person p" +
                onClause("p.age >= d.age")
                + " ORDER BY d.name ASC", crit.getQueryString());
        List<Tuple> results = crit.getResultList();

        assertEquals(2, results.size());
        assertEquals("doc1", results.get(0).get(0));
        assertEquals("pers2", results.get(0).get(1));

        assertEquals("doc2", results.get(1).get(0));
        assertEquals("pers2", results.get(1).get(1));
    }

    @Test
    public void testEntityLeftJoin() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class)
                .from(Document.class, "d")
                .leftJoinOn(Person.class, "p")
                    .on("p.name").eqExpression("d.name")
                .end()
                .select("d.name").select("p.name")
                .orderByAsc("d.name");
        assertEquals("SELECT d.name, p.name FROM Document d LEFT JOIN Person p" +
                onClause("p.name = d.name") +
                " ORDER BY d.name ASC", crit.getQueryString());
        List<Tuple> results = crit.getResultList();

        assertEquals(3, results.size());
        assertEquals("doc1", results.get(0).get(0));
        assertEquals("doc1", results.get(0).get(1));

        assertEquals("doc2", results.get(1).get(0));
        assertNull(results.get(1).get(1));

        assertEquals("doc3", results.get(2).get(0));
        assertNull(results.get(2).get(1));
    }
}
