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
import com.blazebit.persistence.entity.DocumentType;
import com.blazebit.persistence.entity.Person;
import com.blazebit.persistence.internal.RestrictionBuilderExperimental;
import java.util.List;
import javax.persistence.EntityTransaction;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 */
public class JpqlFunctionTest extends AbstractCoreTest {
    
    @Before
    public void setUp(){
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Document doc1 = new Document("D1");
            Document doc2 = new Document("D2");
            Document doc3 = new Document("D3");
           
            Person o1 = new Person("P1");

            doc1.setOwner(o1);
            doc2.setOwner(o1);
            doc3.setOwner(o1);

            em.persist(o1);

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
    public void testLimit(){
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class, "d");
        ((RestrictionBuilderExperimental<CriteriaBuilder<Document>>) cb.where("d.id"))
            .in("subqueryAlias", "(FUNCTION('LIMIT', subqueryAlias, 1))")
                .from(Document.class, "subDoc")
                .select("subDoc.id")
                .orderByAsc("subDoc.name")
                .end();
        String expected = "SELECT d FROM Document d WHERE d.id IN (LIMIT("
                + "(SELECT subDoc.id FROM Document subDoc ORDER BY subDoc.name ASC NULLS LAST)"
                + ",1))";
        
        assertEquals(expected, cb.getQueryString());
        List<Document> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals("D1", resultList.get(0).getName());
    }
    
    @Test
    public void testLimitOffset(){
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class, "d");
        ((RestrictionBuilderExperimental<CriteriaBuilder<Document>>) cb.where("d.id"))
            .in("subqueryAlias", "(FUNCTION('LIMIT', subqueryAlias, 1, 1))")
                .from(Document.class, "subDoc")
                .select("subDoc.id")
                .orderByAsc("subDoc.name")
                .end();
        String expected = "SELECT d FROM Document d WHERE d.id IN (LIMIT("
                + "(SELECT subDoc.id FROM Document subDoc ORDER BY subDoc.name ASC NULLS LAST)"
                + ",1,1))";
        
        assertEquals(expected, cb.getQueryString());
        List<Document> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals("D2", resultList.get(0).getName());
    }
}
