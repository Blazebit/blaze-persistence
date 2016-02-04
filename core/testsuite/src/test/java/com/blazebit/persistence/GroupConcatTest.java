/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence;

import static org.junit.Assert.assertEquals;

import javax.persistence.EntityTransaction;
import javax.persistence.Tuple;

import org.junit.Before;
import org.junit.Test;

import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.Person;
import com.blazebit.persistence.entity.Version;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public class GroupConcatTest extends AbstractCoreTest {
    
    @Before
    public void setUp() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
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

            em.flush();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSimpleWithDefault() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("FUNCTION('GROUP_CONCAT', doc.name, 'ORDER BY', doc.name, 'ASC')")
            .groupBy("owner")
            ;

        Tuple actual = criteria.getResultList().get(0);

        assertEquals("Doc1,Doc1,Doc2", actual.get(0));
    }

    @Test
    public void testSimpleWithSeparator() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("FUNCTION('GROUP_CONCAT', doc.name, 'SEPARATOR', ', ', 'ORDER BY', doc.name, 'ASC')")
            .groupBy("owner")
            ;

        Tuple actual = criteria.getResultList().get(0);

        assertEquals("Doc1, Doc1, Doc2", actual.get(0));
    }

    @Test
    public void testDistinct() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("FUNCTION('GROUP_CONCAT', 'DISTINCT', doc.name, 'SEPARATOR', ', ', 'ORDER BY', doc.name, 'ASC')")
            .groupBy("owner")
            ;

        Tuple actual = criteria.getResultList().get(0);

        assertEquals("Doc1, Doc2", actual.get(0));
    }

    @Test
    public void testDescNullsLast() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("FUNCTION('GROUP_CONCAT', doc.versions.url, 'SEPARATOR', ', ', 'ORDER BY', doc.versions.url, 'DESC NULLS LAST')")
            .groupBy("owner")
            ;

        Tuple actual = criteria.getResultList().get(0);

        assertEquals("b, a", actual.get(0));
    }
}
