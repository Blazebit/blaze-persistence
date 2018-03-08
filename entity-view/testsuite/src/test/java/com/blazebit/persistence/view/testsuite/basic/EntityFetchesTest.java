/*
 * Copyright 2014 - 2018 Blazebit.
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
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate51;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.basic.model.FetchesDocumentView1;
import com.blazebit.persistence.view.testsuite.basic.model.FetchesDocumentView3;
import com.blazebit.persistence.view.testsuite.basic.model.FetchesDocumentView2;
import com.blazebit.persistence.view.testsuite.basic.model.FetchesPersonView1;
import com.blazebit.persistence.view.testsuite.basic.model.FetchesPersonView3;
import com.blazebit.persistence.view.testsuite.basic.model.FetchesPersonView2;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
        cfg.addEntityView(FetchesDocumentView1.class);
        cfg.addEntityView(FetchesPersonView1.class);
        cfg.addEntityView(FetchesDocumentView2.class);
        cfg.addEntityView(FetchesPersonView2.class);
        cfg.addEntityView(FetchesDocumentView3.class);
        cfg.addEntityView(FetchesPersonView3.class);
        evm = cfg.createEntityViewManager(cbf);
    }

    private Person pers1;
    private Person friend;
    private Document partnerDoc;
    private Document doc1;
    private Document doc2;
    private Document doc3;

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
                doc3 = new Document("doc3", friend);
                partnerDoc = new Document("partnerDoc", friend);
                partnerDoc.getContacts().put(0, p6);
                partnerDoc.getContacts().put(1, p7);
                partnerDoc.getPeople().add(pers1);
                partnerDoc.getPeople().add(friend);

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
                em.persist(doc3);
                em.persist(partnerDoc);

                pers1.setPartnerDocument(partnerDoc);
            }
        });

        // Apparently EclipseLink and DataNucleus aren't smart enough to figure out
        // there is only a single entity so we have to use getResultList().get(0) instead of getSingleResult()..
        friend = cbf.create(em, Person.class)
                .fetch("localized", "ownedDocuments")
                .where("id").eq(friend.getId())
                .getResultList().get(0);

        pers1 = cbf.create(em, Person.class)
                .fetch("partnerDocument.contacts.localized", "partnerDocument.people.localized")
                .fetch("ownedDocuments.contacts", "partnerDocument.contacts.ownedDocuments", "partnerDocument.people.ownedDocuments")
                .where("id").eq(pers1.getId())
                .getResultList().get(0);
        doc1 = cbf.create(em, Document.class)
                .where("id").eq(doc1.getId())
                .getResultList().get(0);
        doc2 = cbf.create(em, Document.class)
                .where("id").eq(doc2.getId())
                .getResultList().get(0);
        doc3 = cbf.create(em, Document.class)
                .where("id").eq(doc3.getId())
                .getResultList().get(0);
    }

    @Test
    // NOTE: Datanucleus doesn't seem to support fetching non-root relations
    @Category({ NoDatanucleus.class })
    public void entityViewFetchesInitializeEntitiesSimple() {
        CriteriaBuilder<Person> criteria = cbf.create(em, Person.class)
                .where("id").eq(pers1.getId());
        EntityViewSetting<FetchesPersonView1, CriteriaBuilder<FetchesPersonView1>> setting = EntityViewSetting.create(FetchesPersonView1.class);
        CriteriaBuilder<FetchesPersonView1> cb = evm.applySetting(setting, criteria);
        List<FetchesPersonView1> results = cb.getResultList();

        // Close the em and emf to make sure this was fetched properly
        em.getTransaction().rollback();
        em.close();
        emf.close();

        // Only a single entity view is produced
        assertEquals(1, results.size());
        FetchesPersonView1 view = results.get(0);
        // Friends match
        assertEquals(friend.getId(), view.getFriend().getId());

        // Friend is properly initialized
        assertEquals(2, view.getFriend().getOwnedDocuments().size());
        assertEquals(friend.getOwnedDocuments(), view.getFriend().getOwnedDocuments());

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
        FetchesDocumentView1 partnerDocumentView = view.getPartnerDocument();

        // Check personList is properly initialized
        assertEquals(2, partnerDocumentView.getPeople().size());
        assertEquals(partnerDocument.getPeople(), partnerDocumentView.getPeople());
        assertEquals(pers1, partnerDocumentView.getPeople().get(0));
        assertEquals(2, partnerDocumentView.getPeople().get(0).getOwnedDocuments().size());
        assertEquals(pers1.getOwnedDocuments(), partnerDocumentView.getPeople().get(0).getOwnedDocuments());
        assertEquals(friend, partnerDocumentView.getPeople().get(1));
        assertEquals(2, partnerDocumentView.getPeople().get(1).getOwnedDocuments().size());
        assertEquals(friend.getOwnedDocuments(), partnerDocumentView.getPeople().get(1).getOwnedDocuments());
    }

    @Test
    // NOTE: EclipseLink messes up the whole query result as soon as map values are selected...
    // NOTE: Datanucleus doesn't seem to support fetching non-root relations
    @Category({ NoEclipselink.class, NoDatanucleus.class })
    public void entityViewFetchesInitializeEntities() {
        CriteriaBuilder<Person> criteria = cbf.create(em, Person.class)
            .where("id").eq(pers1.getId());
        EntityViewSetting<FetchesPersonView2, CriteriaBuilder<FetchesPersonView2>> setting = EntityViewSetting.create(FetchesPersonView2.class);
        CriteriaBuilder<FetchesPersonView2> cb = evm.applySetting(setting, criteria);
        List<FetchesPersonView2> results = cb.getResultList();

        // Close the em and emf to make sure this was fetched properly
        em.getTransaction().rollback();
        em.close();
        emf.close();

        // Only a single entity view is produced
        assertEquals(1, results.size());
        FetchesPersonView2 view = results.get(0);
        // Friends match
        assertEquals(friend.getId(), view.getFriend().getId());

        // Friend is properly initialized
        assertEquals(2, view.getFriend().getOwnedDocuments().size());
        assertEquals(friend.getOwnedDocuments(), view.getFriend().getOwnedDocuments());

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
        FetchesDocumentView2 partnerDocumentView = view.getPartnerDocument();

        // Check contacts and personList are properly initialized
        assertEquals(2, partnerDocumentView.getContacts().size());
        assertEquals(partnerDocument.getContacts(), partnerDocumentView.getContacts());

        assertEquals("p6", partnerDocumentView.getContacts().get(0).getName());
        assertEquals(0, partnerDocumentView.getContacts().get(0).getOwnedDocuments().size());
        assertEquals("p7", partnerDocumentView.getContacts().get(1).getName());
        assertEquals(0, partnerDocumentView.getContacts().get(1).getOwnedDocuments().size());

        assertEquals(2, partnerDocumentView.getPeople().size());
        assertEquals(partnerDocument.getPeople(), partnerDocumentView.getPeople());
        assertEquals(pers1, partnerDocumentView.getPeople().get(0));
        assertEquals(2, partnerDocumentView.getPeople().get(0).getOwnedDocuments().size());
        assertEquals(pers1.getOwnedDocuments(), partnerDocumentView.getPeople().get(0).getOwnedDocuments());
        assertEquals(friend, partnerDocumentView.getPeople().get(1));
        assertEquals(2, partnerDocumentView.getPeople().get(1).getOwnedDocuments().size());
        assertEquals(friend.getOwnedDocuments(), partnerDocumentView.getPeople().get(1).getOwnedDocuments());
    }

    @Test
    // NOTE: EclipseLink messes up the whole query result as soon as map values are selected...
    // NOTE: Element collection fetching of non-roots only got fixed in Hibernate 5.2.3: https://hibernate.atlassian.net/browse/HHH-11140
    // NOTE: Datanucleus doesn't seem to support fetching non-root relations
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoEclipselink.class, NoDatanucleus.class })
    public void entityViewFetchesInitializeEntitiesWithElementCollections() {
        CriteriaBuilder<Person> criteria = cbf.create(em, Person.class)
                .where("id").eq(pers1.getId());
        EntityViewSetting<FetchesPersonView3, CriteriaBuilder<FetchesPersonView3>> setting = EntityViewSetting.create(FetchesPersonView3.class);
        CriteriaBuilder<FetchesPersonView3> cb = evm.applySetting(setting, criteria);
        List<FetchesPersonView3> results = cb.getResultList();

        // Close the em and emf to make sure this was fetched properly
        em.getTransaction().rollback();
        em.close();
        emf.close();

        // Only a single entity view is produced
        assertEquals(1, results.size());
        FetchesPersonView3 view = results.get(0);
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
        FetchesDocumentView3 partnerDocumentView = view.getPartnerDocument();

        // Check contacts and personList are properly initialized
        assertEquals(2, partnerDocumentView.getContacts().size());
        assertEquals(partnerDocument.getContacts(), partnerDocumentView.getContacts());
        for (Map.Entry<Integer, Person> entry : partnerDocumentView.getContacts().entrySet()) {
            assertEquals(0, entry.getValue().getLocalized().size());
        }

        assertEquals(2, partnerDocumentView.getPeople().size());
        assertEquals(partnerDocument.getPeople(), partnerDocumentView.getPeople());
        assertEquals(pers1, partnerDocumentView.getPeople().get(0));
        assertEquals(0, partnerDocumentView.getPeople().get(0).getLocalized().size());
        assertEquals(pers1.getLocalized(), partnerDocumentView.getPeople().get(0).getLocalized());
        assertEquals(friend, partnerDocumentView.getPeople().get(1));
        assertEquals(2, partnerDocumentView.getPeople().get(1).getLocalized().size());
        assertEquals(friend.getLocalized(), partnerDocumentView.getPeople().get(1).getLocalized());
    }
}
