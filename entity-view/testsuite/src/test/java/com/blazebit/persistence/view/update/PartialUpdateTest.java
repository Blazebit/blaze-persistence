/*
 * Copyright 2015 Blazebit.
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
package com.blazebit.persistence.view.update;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus;
import com.blazebit.persistence.view.AbstractEntityViewTest;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.entity.Document;
import com.blazebit.persistence.view.entity.Person;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.update.model.PartialDocumentView;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public class PartialUpdateTest extends AbstractEntityViewTest {

    protected static EntityViewManager evm;

    @BeforeClass
    public static void initEvm() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(PartialDocumentView.class);
        evm = cfg.createEntityViewManager();
    }

    private Document doc;

    @Before
    public void setUp() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            doc = new Document("doc");

            Person o1 = new Person("pers1");
            o1.getLocalized().put(1, "localized1");

            doc.setOwner(o1);
            doc.getContacts().put(1, o1);
            doc.getContacts2().put(2, o1);

            em.persist(o1);
            em.persist(doc);
            
            o1.setPartnerDocument(doc);

            em.flush();
            tx.commit();
            em.clear();

            doc = em.find(Document.class, doc.getId());
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
    }

    @Test
    @Category({ NoDatanucleus.class })
    public void testSimpleUpdate() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d").orderByAsc("id");
        CriteriaBuilder<PartialDocumentView> cb = evm.applySetting(EntityViewSetting.create(PartialDocumentView.class), criteria);
        List<PartialDocumentView> results = cb.getResultList();
        PartialDocumentView docView = results.get(0);
        
        // When
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
	        docView.setName("newDoc");
	        evm.update(em, docView);
            em.flush();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }

        // Then
        em.clear();
        doc = em.find(Document.class, doc.getId());
        assertEquals(doc.getName(), docView.getName());
    }

    @Test
    @Category({ NoDatanucleus.class })
    public void testUpdateRollbacked() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d").orderByAsc("id");
        CriteriaBuilder<PartialDocumentView> cb = evm.applySetting(EntityViewSetting.create(PartialDocumentView.class), criteria);
        List<PartialDocumentView> results = cb.getResultList();
        PartialDocumentView docView = results.get(0);
        
        // When
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
	        docView.setName("newDoc");
	        evm.update(em, docView);
            em.flush();
            tx.rollback();

            tx.begin();
	        evm.update(em, docView);
            em.flush();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }

        // Then
        em.clear();
        doc = em.find(Document.class, doc.getId());
        assertEquals(doc.getName(), docView.getName());
    }

    @Test
    @Category({ NoDatanucleus.class })
    public void testModifyAndUpdateRollbacked() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d").orderByAsc("id");
        CriteriaBuilder<PartialDocumentView> cb = evm.applySetting(EntityViewSetting.create(PartialDocumentView.class), criteria);
        List<PartialDocumentView> results = cb.getResultList();
        PartialDocumentView docView = results.get(0);
        
        // When
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
	        docView.setName("newDoc");
	        evm.update(em, docView);
            em.flush();
            tx.rollback();
            
	        docView.setName("newDoc1");
	        docView.setLastModified(new Date());

            tx.begin();
	        evm.update(em, docView);
            em.flush();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }

        // Then
        em.clear();
        doc = em.find(Document.class, doc.getId());
        assertEquals(doc.getName(), docView.getName());
        assertEquals(doc.getLastModified().getTime(), docView.getLastModified().getTime());
    }
}
