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

import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.Person;
import javax.persistence.EntityTransaction;
import javax.persistence.Tuple;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class KeySetPaginationTest extends AbstractCoreTest {

    @Before
    public void setUp() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Document doc1 = new Document("doc1");
            Document doc2 = new Document("doc2");
            Document doc3 = new Document("doc3");

            Person o1 = new Person("Karl1");
            Person o2 = new Person("Karl2");
            Person o3 = new Person("Karl3");

            doc1.setOwner(o1);
            doc2.setOwner(o2);
            doc3.setOwner(o3);

            em.persist(o1);
            em.persist(o2);
            em.persist(o3);

            em.persist(doc1);
            em.persist(doc2);
            em.persist(doc3);

            em.flush();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void simpleTest() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(Document.class, "d")
            .select("d.name").select("d.owner.name");
        crit.orderByDesc("d.owner.name")
            .orderByAsc("d.name")
            .orderByAsc("d.id");
        
        PaginatedCriteriaBuilder<Tuple> pcb = crit.page(null, 0, 1);
        PagedList<Tuple> result = pcb.getResultList();
        // The first time we have to use the offset
        String expectedIdQuery = "SELECT d.id, owner_1.name, d.name, d.id FROM Document d JOIN d.owner owner_1 "
            + "GROUP BY d.id, owner_1.name, d.name, d.id "
            + "ORDER BY owner_1.name DESC NULLS LAST, d.name ASC NULLS LAST, d.id ASC NULLS LAST";
        assertEquals(expectedIdQuery, pcb.getPageIdQueryString());
        
        assertEquals(1, result.size());
        assertEquals(3, result.totalSize());
        assertEquals("doc3", result.get(0).get(0));
        
        pcb = crit.page(result.getKeySet(), 1, 1);
        result = pcb.getResultList();
        // Finally we can use the key set
        expectedIdQuery = "SELECT d.id, owner_1.name, d.name, d.id FROM Document d JOIN d.owner owner_1 "
            + "WHERE (owner_1.name < :_keySetParameter_0 OR (owner_1.name = :_keySetParameter_0 AND (d.name > :_keySetParameter_1 OR (d.name = :_keySetParameter_1 AND d.id > :_keySetParameter_2)))) "
            + "GROUP BY d.id, owner_1.name, d.name, d.id "
            + "ORDER BY owner_1.name DESC NULLS LAST, d.name ASC NULLS LAST, d.id ASC NULLS LAST";
        assertEquals(expectedIdQuery, pcb.getPageIdQueryString());
        
        pcb = crit.page(result.getKeySet(), 1, 1);
        result = pcb.getResultList();
        // Same page again key set
        expectedIdQuery = "SELECT d.id, owner_1.name, d.name, d.id FROM Document d JOIN d.owner owner_1 "
            + "WHERE (owner_1.name <= :_keySetParameter_0 OR (owner_1.name = :_keySetParameter_0 AND (d.name >= :_keySetParameter_1 OR (d.name = :_keySetParameter_1 AND d.id >= :_keySetParameter_2)))) "
            + "GROUP BY d.id, owner_1.name, d.name, d.id "
            + "ORDER BY owner_1.name DESC NULLS LAST, d.name ASC NULLS LAST, d.id ASC NULLS LAST";
        assertEquals(expectedIdQuery, pcb.getPageIdQueryString());
        
        assertEquals(1, result.size());
        assertEquals(3, result.totalSize());
        assertEquals("doc2", result.get(0).get(0));
        
        pcb = crit.page(result.getKeySet(), 0, 1);
        result = pcb.getResultList();
        // Now we scroll back
        expectedIdQuery = "SELECT d.id, owner_1.name, d.name, d.id FROM Document d JOIN d.owner owner_1 "
            + "WHERE (owner_1.name > :_keySetParameter_0 OR (owner_1.name = :_keySetParameter_0 AND (d.name < :_keySetParameter_1 OR (d.name = :_keySetParameter_1 AND d.id < :_keySetParameter_2)))) "
            + "GROUP BY d.id, owner_1.name, d.name, d.id "
            + "ORDER BY owner_1.name ASC NULLS FIRST, d.name DESC NULLS FIRST, d.id DESC NULLS FIRST";
        assertEquals(expectedIdQuery, pcb.getPageIdQueryString());
        
        assertEquals(1, result.size());
        assertEquals(3, result.totalSize());
        assertEquals("doc3", result.get(0).get(0));
    }
}
