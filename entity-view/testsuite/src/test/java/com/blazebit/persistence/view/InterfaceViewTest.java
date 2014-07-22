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

package com.blazebit.persistence.view;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.Person;
import com.blazebit.persistence.spi.Criteria;
import com.blazebit.persistence.view.model.DocumentViewInterface;
import java.util.List;
import javax.persistence.EntityTransaction;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author cpbec
 */
public class InterfaceViewTest extends AbstractEntityViewPersistenceTest {
    
    private Document doc1;
    private Document doc2;
    
    @Before
    public void setUp() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            doc1 = new Document("doc1");
            doc2 = new Document("doc2");
            
            Person o1 = new Person("pers1");
            Person o2 = new Person("pers2");
            o1.getLocalized().put(1, "localized1");
            o2.getLocalized().put(1, "localized2");
            
            doc1.setOwner(o1);
            doc2.setOwner(o2);
            
            doc1.getContacts().put(1, o1);
            doc2.getContacts().put(1, o2);
            
            doc1.getContacts2().put(2, o1);
            doc2.getContacts2().put(2, o2);
            
            em.persist(o1);
            em.persist(o2);
            
            em.persist(doc1);
            em.persist(doc2);
            
            em.flush();
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            tx.rollback();
        }
    }
    
    @Test
    public void testInterface() {
        CriteriaBuilder<Document> criteria = Criteria.from(em, Document.class, "d")
                .orderByAsc("id");
        List<DocumentViewInterface> results = evm.applyObjectBuilder(DocumentViewInterface.class, criteria).setParameter("contactPersonNumber", 2).getResultList(em);
        
        assertEquals(2, results.size());
        // Doc1
        assertEquals(doc1.getId(), results.get(0).getId());
        assertEquals(doc1.getName(), results.get(0).getName());
        assertEquals(doc1.getContacts().get(1), results.get(0).getFirstContactPerson());
        assertEquals(doc1.getContacts2().get(2), results.get(0).getMyContactPerson());
        // Doc2
        assertEquals(doc2.getId(), results.get(1).getId());
        assertEquals(doc2.getName(), results.get(1).getName());
        assertEquals(doc2.getContacts().get(1), results.get(1).getFirstContactPerson());
        assertEquals(doc2.getContacts2().get(2), results.get(1).getMyContactPerson());
    }
}
