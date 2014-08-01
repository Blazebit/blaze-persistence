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

package com.blazebit.persistence.view.subview;

import com.blazebit.persistence.AbstractPersistenceTest;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.Person;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.impl.EntityViewConfigurationImpl;
import com.blazebit.persistence.view.subview.model.DocumentMasterView;
import com.blazebit.persistence.view.subview.model.PersonSubView;
import com.blazebit.persistence.view.subview.model.PersonSubViewFiltered;
import java.util.List;
import javax.persistence.EntityTransaction;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author cpbec
 */
public class NullSubviewTest extends AbstractPersistenceTest {
    
    private Document doc1;
    
    @Before
    public void setUp() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            doc1 = new Document("doc1");
            
            Person o1 = new Person("pers1");
            o1.getLocalized().put(1, "localized1");
            
            doc1.setOwner(o1);
            
            em.persist(o1);
            
            em.persist(doc1);
            
            em.flush();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
    }
    
    @Test
    public void testSubview() {
        EntityViewConfigurationImpl cfg = new EntityViewConfigurationImpl();
        cfg.addEntityView(DocumentMasterView.class);
        cfg.addEntityView(PersonSubView.class);
        cfg.addEntityView(PersonSubViewFiltered.class);
        EntityViewManager evm = cfg.createEntityViewManager();
        
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d")
                .orderByAsc("id");
        CriteriaBuilder<DocumentMasterView> cb = evm.applyObjectBuilder(DocumentMasterView.class, criteria)
                .setParameter("contactPersonNumber", 2);
        System.out.println(cb.getQueryString());
        List<DocumentMasterView> results = cb.getResultList();
        
        assertEquals(1, results.size());
        DocumentMasterView res = results.get(0);
        // Doc1
        assertEquals(doc1.getName(), res.getName());
        // Subview
        assertEquals("pers1", res.getOwner().getName());
        // Filtered subview
        assertNull(results.get(0).getMyContactPerson());
        // Map subview
        assertTrue(results.get(0).getContacts().isEmpty());
        // Set subview
        assertTrue(results.get(0).getPartners().isEmpty());
        // List subview
        assertTrue(results.get(0).getPersonList().isEmpty());
    }
}
