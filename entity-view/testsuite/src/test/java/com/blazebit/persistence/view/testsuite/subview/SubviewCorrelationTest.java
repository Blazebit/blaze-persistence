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
package com.blazebit.persistence.view.testsuite.subview;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.entity.Document;
import com.blazebit.persistence.view.testsuite.entity.Person;
import com.blazebit.persistence.view.testsuite.subview.model.*;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityTransaction;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class SubviewCorrelationTest extends AbstractEntityViewTest {

    private Document doc1;
    private Document doc2;
    private Document doc3;
    private Document doc4;

    @Before
    public void setUp() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            doc1 = new Document("doc1");
            doc2 = new Document("doc2");
            doc3 = new Document("doc3");
            doc4 = new Document("doc4");

            Person o1 = new Person("pers1");
            Person o2 = new Person("pers2");
            Person o3 = new Person("pers3");

            doc1.setOwner(o1);
            doc2.setOwner(o2);
            doc3.setOwner(o2);
            doc4.setOwner(o2);

            em.persist(o1);
            em.persist(o2);
            em.persist(o3);

            em.persist(doc1);
            em.persist(doc2);
            em.persist(doc3);
            em.persist(doc4);

            em.flush();
            tx.commit();
            em.clear();

            doc1 = em.find(Document.class, doc1.getId());
            doc2 = em.find(Document.class, doc2.getId());
            doc3 = em.find(Document.class, doc3.getId());
            doc4 = em.find(Document.class, doc4.getId());
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
    }
    @Test
    public void testSubqueryCorrelation() {
        testCorrelation(DocumentCorrelationViewSubquery.class);
    }

//    @Test
//    public void testBatchCorrelation() {
//        testCorrelation(DocumentCorrelationViewJoin.class);
//    }
//
//    @Test
//    public void testJoinCorrelation() {
//        testCorrelation(DocumentCorrelationViewJoin.class);
//    }

    private <T extends DocumentCorrelationView> void testCorrelation(Class<T> entityView) {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(entityView);
        cfg.addEntityView(DocumentRelatedView.class);
        EntityViewManager evm = cfg.createEntityViewManager(cbf, em.getEntityManagerFactory());

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d").orderByAsc("id");
        CriteriaBuilder<T> cb = evm.applySetting(EntityViewSetting.create(entityView), criteria);
        List<T> results = cb.getResultList();

        assertEquals(4, results.size());
        // Doc1
        assertEquals(doc1.getName(), results.get(0).getName());
        assertEquals(0, results.get(0).getOwnerRelatedDocuments().size());
        assertEquals(0, results.get(0).getOwnerRelatedDocumentIds().size());

        // Doc2
        assertEquals(doc2.getName(), results.get(1).getName());
        assertEquals(2, results.get(1).getOwnerRelatedDocuments().size());
        assertEquals(doc3.getName(), results.get(1).getOwnerRelatedDocuments().get(0).getName());
        assertEquals(doc4.getName(), results.get(1).getOwnerRelatedDocuments().get(1).getName());
        assertEquals(2, results.get(1).getOwnerRelatedDocumentIds().size());
        assertEquals(doc3.getId(), results.get(1).getOwnerRelatedDocumentIds().get(0));
        assertEquals(doc4.getId(), results.get(1).getOwnerRelatedDocumentIds().get(1));

        // Doc3
        assertEquals(doc3.getName(), results.get(2).getName());
        assertEquals(2, results.get(2).getOwnerRelatedDocuments().size());
        assertEquals(doc2.getName(), results.get(2).getOwnerRelatedDocuments().get(0).getName());
        assertEquals(doc4.getName(), results.get(2).getOwnerRelatedDocuments().get(1).getName());
        assertEquals(2, results.get(2).getOwnerRelatedDocumentIds().size());
        assertEquals(doc2.getId(), results.get(2).getOwnerRelatedDocumentIds().get(0));
        assertEquals(doc4.getId(), results.get(2).getOwnerRelatedDocumentIds().get(1));

        // Doc4
        assertEquals(doc4.getName(), results.get(3).getName());
        assertEquals(2, results.get(3).getOwnerRelatedDocuments().size());
        assertEquals(doc2.getName(), results.get(3).getOwnerRelatedDocuments().get(0).getName());
        assertEquals(doc3.getName(), results.get(3).getOwnerRelatedDocuments().get(1).getName());
        assertEquals(2, results.get(3).getOwnerRelatedDocumentIds().size());
        assertEquals(doc2.getId(), results.get(3).getOwnerRelatedDocumentIds().get(0));
        assertEquals(doc3.getId(), results.get(3).getOwnerRelatedDocumentIds().get(1));
    }
}
