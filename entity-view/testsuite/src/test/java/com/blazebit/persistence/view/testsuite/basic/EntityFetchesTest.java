/*
 * Copyright 2014 - 2017 Blazebit.
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

package com.blazebit.persistence.view.testsuite.basic;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.basic.model.EmbeddableTestEntityEmbeddableSubView;
import com.blazebit.persistence.view.testsuite.basic.model.EmbeddableTestEntitySimpleEmbeddableSubView;
import com.blazebit.persistence.view.testsuite.basic.model.EmbeddableTestEntitySubView;
import com.blazebit.persistence.view.testsuite.basic.model.EmbeddableTestEntityView;
import com.blazebit.persistence.view.testsuite.basic.model.EmbeddableTestEntityViewWithSubview;
import com.blazebit.persistence.view.testsuite.basic.model.FetchesDocumentView;
import com.blazebit.persistence.view.testsuite.basic.model.FetchesPersonView;
import com.blazebit.persistence.view.testsuite.basic.model.IntIdEntityView;
import com.blazebit.persistence.view.testsuite.entity.Document;
import com.blazebit.persistence.view.testsuite.entity.EmbeddableTestEntity;
import com.blazebit.persistence.view.testsuite.entity.EmbeddableTestEntityId;
import com.blazebit.persistence.view.testsuite.entity.EmbeddableTestEntitySimpleEmbeddable;
import com.blazebit.persistence.view.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.view.testsuite.entity.Person;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EntityFetchesTest extends AbstractEntityViewTest {

    protected EntityViewManager evm;
    
    @Before
    public void initEvm() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(FetchesDocumentView.class);
        cfg.addEntityView(FetchesPersonView.class);
        evm = cfg.createEntityViewManager(cbf);
    }

    private Person pers1;
    private Person friend;
    private Document partnerDoc;
    private Document doc1;
    private Document doc2;

    @Before
    public void setUp() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                pers1 = new Person("pers1");
                friend = new Person("friend");
                friend.getLocalized().put(0, "Zero");
                friend.getLocalized().put(1, "Null");
                pers1.setFriend(friend);

                Person p2, p3, p4, p5, p6, p7;
                p2 = new Person("p2");
                p3 = new Person("p3");
                p4 = new Person("p4");
                p5 = new Person("p5");
                p6 = new Person("p6");
                p7 = new Person("p7");

                doc1 = new Document("doc1", pers1);
                doc1.getContacts().put(0, p2);
                doc1.getContacts().put(1, p3);
                doc2 = new Document("doc2", pers1);
                doc2.getContacts().put(0, p4);
                doc2.getContacts().put(1, p5);
                partnerDoc = new Document("partnerDoc", friend);
                partnerDoc.getContacts().put(0, p6);
                partnerDoc.getContacts().put(1, p7);
                partnerDoc.getPersonList().add(pers1);
                partnerDoc.getPersonList().add(friend);

                em.persist(friend);
                em.persist(pers1);
                em.persist(p2);
                em.persist(p3);
                em.persist(p4);
                em.persist(p5);
                em.persist(p6);
                em.persist(p7);
                em.persist(doc1);
                em.persist(doc2);
                em.persist(partnerDoc);

                pers1.setPartnerDocument(partnerDoc);
            }
        });

        // Apparently EclipseLink and DataNucleus aren't smart enough to figure out
        // there is only a single entity so we have to use getResultList().get(0) instead of getSingleResult()..
        friend = cbf.create(em, Person.class)
                .fetch("localized")
                .where("id").eq(friend.getId())
                .getResultList().get(0);

        pers1 = cbf.create(em, Person.class)
                .fetch("ownedDocuments.contacts", "partnerDocument.contacts", "partnerDocument.personList")
                .where("id").eq(pers1.getId())
                .getResultList().get(0);
        doc1 = cbf.create(em, Document.class)
                .where("id").eq(doc1.getId())
                .getResultList().get(0);
        doc2 = cbf.create(em, Document.class)
                .where("id").eq(doc2.getId())
                .getResultList().get(0);
    }

    @Test
    // EclipseLink messes up the selecting of map values...
    @Category({ NoEclipselink.class })
    public void entityViewFetchesInitializeEntities() {
        CriteriaBuilder<Person> criteria = cbf.create(em, Person.class)
            .where("id").eq(pers1.getId());
        EntityViewSetting<FetchesPersonView, CriteriaBuilder<FetchesPersonView>> setting = EntityViewSetting.create(FetchesPersonView.class);
        CriteriaBuilder<FetchesPersonView> cb = evm.applySetting(setting, criteria);
        List<FetchesPersonView> results = cb.getResultList();

        // Close the em and emf to make sure this was fetched properly
        em.close();
        emf.close();

        // Only a single entity view is produced
        assertEquals(1, results.size());
        FetchesPersonView view = results.get(0);
        // Friends match
        assertEquals(friend.getId(), view.getFriend().getId());

        // Friend is properly initialized
        assertEquals(2, view.getFriend().getLocalized().size());
        assertEquals(friend.getLocalized(), view.getFriend().getLocalized());

        // Owned documents have the correct size
        assertEquals(2, view.getOwnedDocuments().size());
        // Extract ownedDocuments from view
        Iterator<Document> documentIterator = view.getOwnedDocuments().iterator();
        Document ownedDoc1 = documentIterator.next();
        Document ownedDoc2 = documentIterator.next();

        // Extract ownedDocuments from pers1
        documentIterator = pers1.getOwnedDocuments().iterator();
        Document expectedDoc1 = documentIterator.next();
        Document expectedDoc2;

        if (!expectedDoc1.getId().equals(ownedDoc1.getId())) {
            expectedDoc2 = expectedDoc1;
            expectedDoc1 = documentIterator.next();
        } else {
            expectedDoc2 = documentIterator.next();
        }

        // Check contacts have been initialized of owned documents
        assertEquals(2, ownedDoc1.getContacts().size());
        assertEquals(expectedDoc1.getContacts(), ownedDoc1.getContacts());

        assertEquals(2, ownedDoc2.getContacts().size());
        assertEquals(expectedDoc2.getContacts(), ownedDoc2.getContacts());

        // Extract partner documents
        Document partnerDocument = pers1.getPartnerDocument();
        FetchesDocumentView partnerDocumentView = view.getPartnerDocument();

        // Check contacts and personList are properly initialized
        assertEquals(2, partnerDocumentView.getContacts().size());
        assertEquals(partnerDocument.getContacts(), partnerDocumentView.getContacts());

        assertEquals(2, partnerDocumentView.getPersonList().size());
        assertEquals(partnerDocument.getPersonList(), partnerDocumentView.getPersonList());
    }
}
