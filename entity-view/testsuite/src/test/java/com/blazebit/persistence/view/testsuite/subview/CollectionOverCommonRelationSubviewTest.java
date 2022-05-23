/*
 * Copyright 2014 - 2022 Blazebit.
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
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.subview.model.DocumentMasterView;
import com.blazebit.persistence.view.testsuite.subview.model.DocumentRelatedView;
import com.blazebit.persistence.view.testsuite.subview.model.DocumentWithCollectionOverCommonRelationView;
import com.blazebit.persistence.view.testsuite.subview.model.PersonSubView;
import com.blazebit.persistence.view.testsuite.subview.model.PersonSubViewFiltered;
import com.blazebit.persistence.view.testsuite.subview.model.SimpleDocumentView;
import com.blazebit.persistence.view.testsuite.subview.model.SimplePersonSubView;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class CollectionOverCommonRelationSubviewTest extends AbstractEntityViewTest {

    private Document doc1;
    private Document doc2;
    private Document doc3;

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                doc1 = new Document("doc1");
                doc2 = new Document("doc2");
                doc3 = new Document("doc3");

                Person o1 = new Person("pers1");

                doc1.setOwner(o1);
                doc2.setOwner(o1);
                doc3.setOwner(o1);
                doc1.setResponsiblePerson(o1);

                em.persist(o1);

                em.persist(doc1);
                em.persist(doc2);
                em.persist(doc3);
            }
        });
    }

    @Test
    public void testSubview() {
        EntityViewManager evm = build(
                DocumentWithCollectionOverCommonRelationView.class,
                DocumentRelatedView.class,
                SimplePersonSubView.class
        );

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d")
            .orderByAsc("id");
        CriteriaBuilder<DocumentWithCollectionOverCommonRelationView> cb = evm.applySetting(EntityViewSetting.create(DocumentWithCollectionOverCommonRelationView.class), criteria);
        List<DocumentWithCollectionOverCommonRelationView> results = cb.getResultList();

        assertEquals(3, results.size());
//        DocumentMasterView res = results.get(0);
//        // Doc1
//        assertEquals(doc1.getName(), res.getName());
//        assertEquals("PERS1", res.getOwner().getName());
//        assertEquals(Integer.valueOf(2), res.getContactPersonNumber());
//        assertEquals(Integer.valueOf(2), res.getTheContactPersonNumber());
//        // Filtered subview
//        assertNull(res.getMyContactPerson());
//
//        assertTrue(res.getContacts().isEmpty());
//        assertTrue(res.getPartners().isEmpty());
//        assertTrue(res.getPeople().isEmpty());
    }
}
