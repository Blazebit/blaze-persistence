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

package com.blazebit.persistence.view.collections;

import com.blazebit.persistence.AbstractPersistenceTest;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.Person;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.collections.model.DocumentCollectionsView;
import com.blazebit.persistence.view.impl.EntityViewConfigurationImpl;
import com.blazebit.persistence.view.subview.model.PersonSubView;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityTransaction;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author cpbec
 */
public class CollectionsTest extends AbstractPersistenceTest {
    
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
            Person o3 = new Person("pers3");
            Person o4 = new Person("pers4");
            o1.getLocalized().put(1, "localized1");
            o2.getLocalized().put(1, "localized2");
            o1.setPartnerDocument(doc1);
            o2.setPartnerDocument(doc2);
            o3.setPartnerDocument(doc1);
            o4.setPartnerDocument(doc2);
            
            doc1.setOwner(o1);
            doc2.setOwner(o2);
            
            doc1.getContacts().put(2, o1);
            doc2.getContacts().put(2, o2);
            
            doc1.getContacts2().put(1, o1);
            doc2.getContacts2().put(1, o2);
            doc1.getContacts2().put(2, o3);
            doc2.getContacts2().put(2, o4);
            
            em.persist(o1);
            em.persist(o2);
            em.persist(o3);
            em.persist(o4);
            
            doc1.getPartners().add(o1);
            doc1.getPartners().add(o3);
            doc2.getPartners().add(o2);
            doc2.getPartners().add(o4);
            
            doc1.getPersonList().add(o1);
            doc1.getPersonList().add(o2);
            doc2.getPersonList().add(o3);
            doc2.getPersonList().add(o4);
            
            em.persist(doc1);
            em.persist(doc2);
            
            em.flush();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
    }
    
    @Test
    public void testCollections() {
        EntityViewConfigurationImpl cfg = new EntityViewConfigurationImpl();
        cfg.addEntityView(DocumentCollectionsView.class);
        EntityViewManager evm = cfg.createEntityViewManager();
        
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d")
                .orderByAsc("id");
        CriteriaBuilder<DocumentCollectionsView> cb = evm.applyObjectBuilder(DocumentCollectionsView.class, criteria);
        List<DocumentCollectionsView> results = cb.getResultList();
        
        assertEquals(2, results.size());
        // Doc1
        assertEquals(doc1.getName(), results.get(0).getName());
        // Map subview
        assertEquals(doc1.getContacts2().get(1).getName(), results.get(0).getContacts().get(1).getName());
        assertEquals(doc1.getContacts2().get(2).getName(), results.get(0).getContacts().get(2).getName());
        // Set subview
        assertEquals(doc1.getPartners(), results.get(0).getPartners());
        // List subview
        assertEquals(doc1.getPersonList(), results.get(0).getPersonList());
        
        // Doc2
        assertEquals(doc2.getName(), results.get(1).getName());
        // Map subview
        assertEquals(doc2.getContacts2().get(1).getName(), results.get(1).getContacts().get(1).getName());
        assertEquals(doc2.getContacts2().get(2).getName(), results.get(1).getContacts().get(2).getName());
        // Set subview
        assertEquals(doc2.getPartners(), results.get(1).getPartners());
        // List subview
        assertEquals(doc2.getPersonList(), results.get(1).getPersonList());
    }
}
