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

package com.blazebit.persistence.view.subquery;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.Person;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.basic.AbstractEntityViewPersistenceTest;
import com.blazebit.persistence.view.subquery.model.DocumentWithSubquery;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Christian
 */
public class MappingSubqueryTest extends AbstractEntityViewPersistenceTest {
    
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
            o1.getLocalized().put(1, "localized1");
            o2.getLocalized().put(1, "localized2");
            o3.getLocalized().put(1, "localized3");
            o1.setPartnerDocument(doc1);
            o2.setPartnerDocument(doc2);
            o3.setPartnerDocument(doc2);
            
            doc1.setAge(10);
            doc1.setOwner(o1);
            doc2.setAge(20);
            doc2.setOwner(o2);
            
            doc1.getContacts().put(1, o1);
            doc2.getContacts().put(1, o2);
            
            doc1.getContacts2().put(2, o1);
            doc2.getContacts2().put(2, o2);
            
            em.persist(o1);
            em.persist(o2);
            em.persist(o3);
            
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
    public void testSubquery() {
        CriteriaBuilder<Document> cb = cbf.from(em, Document.class).orderByAsc("id");
        List<DocumentWithSubquery> list = evm.applyObjectBuilder(DocumentWithSubquery.class, cb).getResultList();
        
        assertEquals(2, list.size());
        assertEquals("doc1", list.get(0).getName());
        assertEquals(Long.valueOf(1), list.get(0).getContactCount());
        assertEquals("doc2", list.get(1).getName());
        assertEquals(Long.valueOf(2), list.get(1).getContactCount());
    }
    
    @Test
    public void testSubqueryEntityViewSettings() {
        CriteriaBuilder<Document> cb = cbf.from(em, Document.class).orderByDesc("id");
        EntityViewSetting<DocumentWithSubquery> setting = new EntityViewSetting<DocumentWithSubquery>(DocumentWithSubquery.class, 0, 1);
        setting.addAttributeFilter("contactCount", "0");
        PagedList<DocumentWithSubquery> list = setting.apply(evm, cb).getResultList();
        
        assertEquals(1, list.size());
        assertEquals(2, list.totalSize());
        assertEquals("doc2", list.get(0).getName());
        assertEquals(Long.valueOf(2), list.get(0).getContactCount());
    }
}