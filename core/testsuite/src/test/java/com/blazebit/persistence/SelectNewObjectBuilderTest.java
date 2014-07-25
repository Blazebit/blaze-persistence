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

import static com.blazebit.persistence.AbstractPersistenceTest.em;
import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.Version;
import java.util.List;
import javax.persistence.EntityTransaction;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Christian
 */
public class SelectNewObjectBuilderTest extends AbstractPersistenceTest {
    
    @Before
    public void setUp() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Version v1 = new Version();
            Version v2 = new Version();
            Version v3 = new Version();
            em.persist(v1);
            em.persist(v2);
            em.persist(v3);
            em.persist(new Document("Doc1", v1, v3));
            em.persist(new Document("Doc2", v2));

            em.flush();
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            tx.rollback();
        }
    }

    @Test
    public void testSelectNewDocumentObjectBuilder() {
        CriteriaBuilder<String[]> criteria = cbf.from(em, Document.class, "d")
            .selectNew(new ObjectBuilder<String[]>() {

            @Override
            public String[] getExpressions() {
                return new String[] { "name", "UPPER(name)", "LOWER(name)" };
            }

            @Override
            public String[] build(Object[] tuple, String[] aliases) {
                return new String[] { (String) tuple[0], (String) tuple[1], (String) tuple[2] };
            }

            @Override
            public List<String[]> buildList(List<String[]> list) {
                return list;
            }
        });
        assertEquals("SELECT d.name, UPPER(d.name), LOWER(d.name) FROM Document d", criteria.getQueryString());
        List<String[]> actual = criteria.getQuery().getResultList();

        assertArrayEquals(new String[] {"Doc1", "DOC1", "doc1"}, actual.get(0));
        assertArrayEquals(new String[] {"Doc2", "DOC2", "doc2"}, actual.get(1));
    }
}
