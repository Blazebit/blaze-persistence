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
import com.blazebit.persistence.model.DocumentViewModel;
import javax.persistence.EntityTransaction;
import javax.persistence.Tuple;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author ccbem
 */
public class PaginationTest extends AbstractCoreTest {

    @Before
    public void setUp() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Document doc1 = new Document("doc1");
            Document doc2 = new Document("Doc2");
            Document doc3 = new Document("doC3");
            Document doc4 = new Document("dOc4");
            Document doc5 = new Document("DOC5");
            Document doc6 = new Document("bdoc");
            Document doc7 = new Document("adoc");
            
            Person o1 = new Person("Karl1");
            Person o2 = new Person("Karl2");
            o1.getLocalized().put(1, "abra kadabra");
            o2.getLocalized().put(1, "ass");
            
            doc1.setOwner(o1);
            doc2.setOwner(o1);
            doc3.setOwner(o1);
            doc4.setOwner(o2);
            doc5.setOwner(o2);
            doc6.setOwner(o2);
            doc7.setOwner(o2);
            
            doc1.getContacts().put(1, o1);
            doc1.getContacts().put(2, o2);
            
            em.persist(o1);
            em.persist(o2);
            
            em.persist(doc1);
            em.persist(doc2);
            em.persist(doc3);
            em.persist(doc4);
            em.persist(doc5);
            em.persist(doc6);
            em.persist(doc7);
            
            em.flush();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
    }
    
    @Test
    public void simpleTest() {
        CriteriaBuilder<DocumentViewModel> crit = cbf.from(em, Document.class, "d")
                .selectNew(DocumentViewModel.class)
                    .with("d.name")
                    .with("CONCAT(d.owner.name, ' user')")
                    .with("COALESCE(d.owner.localized[1],'no item')")
                    .with("d.owner.partnerDocument.name")
                .end();
        crit.where("d.name").like("doc%", false, null);
        crit.where("d.owner.name").like("%arl%", true, null);
        crit.where("d.owner.localized[1]").like("a%", false, null);
        crit.orderByAsc("d.id");
                
        // do not include joins that are only needed for the select clause
        String expectedCountQuery = "SELECT COUNT(d.id) FROM Document d JOIN d.owner owner LEFT JOIN owner.localized localized " + ON_CLAUSE + " KEY(localized) = 1 "
                + "WHERE UPPER(d.name) LIKE UPPER(:param_0) AND owner.name LIKE :param_1 AND UPPER(" + joinAliasValue("localized") + ") LIKE UPPER(:param_2)";
        
        // limit this query using setFirstResult() and setMaxResult() according to the parameters passed to page()
        String expectedIdQuery = "SELECT DISTINCT d.id FROM Document d JOIN d.owner owner LEFT JOIN owner.localized localized " + ON_CLAUSE + " KEY(localized) = 1 "
                + "WHERE UPPER(d.name) LIKE UPPER(:param_0) AND owner.name LIKE :param_1 AND UPPER(" + joinAliasValue("localized") + ") LIKE UPPER(:param_2) "
                + "ORDER BY d.id ASC NULLS LAST";
        
        String expectedObjectQuery = "SELECT d.name, CONCAT(owner.name,' user'), COALESCE(" + joinAliasValue("localized") + ",'no item'), partnerDocument.name FROM Document d "
                + "JOIN d.owner owner LEFT JOIN owner.localized localized " + ON_CLAUSE + " KEY(localized) = 1 LEFT JOIN owner.partnerDocument partnerDocument "
                + "WHERE d.id IN :ids "
                + "ORDER BY d.id ASC NULLS LAST";
        
        PaginatedCriteriaBuilder<DocumentViewModel> pcb = crit.page(0, 2);
        
        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedIdQuery, pcb.getPageIdQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());
        
        PagedList<DocumentViewModel> result = pcb.getResultList();
        assertEquals(2, result.size());
        assertEquals(5, result.totalSize());
        assertEquals("doc1", result.get(0).getName());
        assertEquals("Doc2", result.get(1).getName());
        
        result = crit.page(2, 2).getResultList();
        assertEquals("doC3", result.get(0).getName());
        assertEquals("dOc4", result.get(1).getName());
        
        result = crit.page(4, 2).getResultList();
        assertEquals(result.size(), 1);
        assertEquals("DOC5", result.get(0).getName());
    }
    
    @Test
    public void testSelectIndexedWithParameter() {
        String expectedCountQuery = "SELECT COUNT(d.id) FROM Document d JOIN d.owner owner WHERE owner.name = :param_0";
        String expectedIdQuery = "SELECT DISTINCT d.id FROM Document d JOIN d.owner owner WHERE owner.name = :param_0";
        String expectedObjectQuery = "SELECT contacts.name FROM Document d LEFT JOIN d.contacts contacts " + ON_CLAUSE + " KEY(contacts) = :contactNr JOIN d.owner owner WHERE d.id IN :ids";
        PaginatedCriteriaBuilder<Tuple> cb = cbf.from(em, Document.class, "d")
            .where("owner.name").eq("Karl1")
                .select("contacts[:contactNr].name")
                .page(0, 1);
        assertEquals(expectedCountQuery, cb.getPageCountQueryString());
        assertEquals(expectedIdQuery, cb.getPageIdQueryString());
        assertEquals(expectedObjectQuery, cb.getQueryString());
    }
    
    @Test
    public void testSelectEmptyResultList() {
        PaginatedCriteriaBuilder<Document> cb = cbf.from(em, Document.class, "d")
            .where("name").isNull()
                .page(0, 1);
        assertEquals(0, cb.getResultList().size());
    }
    
    @Test
    public void testOrderByExpression() {
        PaginatedCriteriaBuilder<Document> cb = cbf.from(em, Document.class, "d")
            .orderByAsc("contacts[:contactNr].name")
            .setParameter("contactNr", 1)
            .page(0, 1);
        String expectedIdQuery = "SELECT DISTINCT d.id FROM Document d LEFT JOIN d.contacts contacts " + ON_CLAUSE + " KEY(contacts) = :contactNr ORDER BY contacts.name ASC NULLS LAST";
        assertEquals(expectedIdQuery, cb.getPageIdQueryString());
    }
    
    @Test
    public void testOrderBySelectAlias() {
        PaginatedCriteriaBuilder<Tuple> cb = cbf.from(em, Document.class, "d")
            .select("contacts[:contactNr].name", "contactName")
            .orderByAsc("contactName")
            .setParameter("contactNr", 1)
            .page(0, 1);
        String expectedIdQuery = "SELECT DISTINCT d.id FROM Document d LEFT JOIN d.contacts contacts " + ON_CLAUSE + " KEY(contacts) = :contactNr ORDER BY contacts.name ASC NULLS LAST";
        assertEquals(expectedIdQuery, cb.getPageIdQueryString());
    }
}
