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
import com.blazebit.persistence.entity.Version;
import com.blazebit.persistence.model.DocumentViewModel;
import com.blazebit.persistence.spi.Criteria;
import java.util.List;
import javax.persistence.EntityTransaction;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author ccbem
 */
public class SelectNewTest extends AbstractPersistenceTest {
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
    public void testSelectNewDocumentViewModel() {
        CriteriaBuilder<DocumentViewModel> criteria = Criteria.from(em, Document.class)
                .selectNew(DocumentViewModel.class).with("name").end().orderByAsc("name");

        assertEquals("SELECT document.name FROM Document document ORDER BY document.name ASC NULLS LAST", criteria.getQueryString());
        List<DocumentViewModel> actual = criteria.getQuery().getResultList();

        /* expected */
        List<Document> expected = em.createQuery("FROM Document d ORDER BY d.name ASC", Document.class).getResultList();

        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            assertEquals(actual.get(i).getName(), expected.get(i).getName());
        }
    }

    @Test
    public void testSelectNewDocument() {
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
        criteria.selectNew(Document.class).with("d.name").end().where("LENGTH(d.name)").le(4).orderByAsc("d.name");
        assertEquals("SELECT d.name FROM Document d WHERE LENGTH(d.name) <= :param_0 ORDER BY d.name ASC NULLS LAST", criteria.getQueryString());
        List<Document> actual = criteria.getQuery().getResultList();

        /* expected */
        List<Document> expected = em.createQuery("FROM Document d ORDER BY d.name ASC", Document.class).getResultList();

        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            assertEquals(actual.get(i).getName(), expected.get(i).getName());
        }
    }

    @Test
    public void testTest() {
        Person p = new Person();
        p.getLocalized().put(2, "JUHU");
        em.persist(p);

        Document d = new Document("Doc1");
        d.getPartners().add(p);
        d.getContacts().put(1, p);
        em.persist(d);
        List<String> expected = (List<String>) em.createQuery("SELECT VALUE(x.localized) FROM Document d LEFT JOIN d.contacts x WHERE KEY(x)=1 AND KEY(x.localized)=2").getResultList();

        System.out.println(expected);
//        assertEquals(expected.size(), actual.size());
//        for(int i = 0; i < actual.size(); i++){
//            assertEquals(actual.get(i).getName(), expected.get(i).getName());
//        }
    }

//    
//    @Test
//    public void testSelectNewModel(){
//        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
//        criteria.selectNew(Document.class).with("d.author.name").end().where("d.title.length").lt(4);
//        
//        
//        assertEquals("SELECT NEW " + Document.class.getName() + "(author.name) FROM Document d LEFT JOIN d.author author LEFT JOIN d.title title WHERE title.legnth < :param_0", criteria.getQueryString());
//    }
//    
//    @Test(expected = NullPointerException.class)
//    public void testSelectNewNullClass(){
//        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
//        criteria.selectNew((Class<Document>)null);        
//    }
//    
//    @Test(expected = NullPointerException.class)
//    public void testSelectNewNullConstructor(){
//        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d");
//        criteria.selectNew((Constructor<Document>)null);        
//    }
}
