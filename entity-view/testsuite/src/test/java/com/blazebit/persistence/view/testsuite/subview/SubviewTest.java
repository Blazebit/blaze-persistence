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

package com.blazebit.persistence.view.testsuite.subview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.testsuite.subview.model.SimpleDocumentView;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.testsuite.subview.model.DocumentMasterView;
import com.blazebit.persistence.view.testsuite.subview.model.PersonSubView;
import com.blazebit.persistence.view.testsuite.subview.model.PersonSubViewFiltered;
import org.junit.experimental.categories.Category;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class SubviewTest extends AbstractEntityViewTest {

    private Document doc1;
    private Document doc2;

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
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

                doc1.getPeople().add(o1);
                doc1.getPeople().add(o2);
                doc2.getPeople().add(o3);
                doc2.getPeople().add(o4);

                em.persist(doc1);
                em.persist(doc2);
            }
        });
    }

    @Before
    public void setUp() {
        doc1 = cbf.create(em, Document.class).where("name").eq("doc1").getSingleResult();
        doc2 = cbf.create(em, Document.class).where("name").eq("doc2").getSingleResult();
    }

    @Test
    @Category({ NoEclipselink.class })
    // Eclipselink has a result set mapping bug in case of map keys
    public void testSubview() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(DocumentMasterView.class);
        cfg.addEntityView(SimpleDocumentView.class);
        cfg.addEntityView(PersonSubView.class);
        cfg.addEntityView(PersonSubViewFiltered.class);
        EntityViewManager evm = cfg.createEntityViewManager(cbf);

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d")
            .orderByAsc("id");
        CriteriaBuilder<DocumentMasterView> cb = evm.applySetting(EntityViewSetting.create(DocumentMasterView.class), criteria)
            .setParameter("contactPersonNumber", 2);
        List<DocumentMasterView> results = cb.getResultList();

        assertEquals(2, results.size());
        // Doc1
        assertEquals(doc1.getName(), results.get(0).getName());
        assertEquals("PERS1", results.get(0).getOwner().getName());
        assertEquals(Integer.valueOf(2), results.get(0).getContactPersonNumber());
        assertEquals(Integer.valueOf(2), results.get(0).getTheContactPersonNumber());
        // Filtered subview
        assertEquals(doc1.getContacts().get(2).getName(), results.get(0).getMyContactPerson().getName());
        assertEquals(Integer.valueOf(2), results.get(0).getMyContactPerson().getContactPersonNumber());

        assertSubviewEquals(doc1, doc1.getContacts2(), results.get(0).getContacts());
        assertSubviewEquals(doc1.getPartners(), results.get(0).getPartners());
        assertSubviewEquals(doc1.getPeople(), results.get(0).getPeople());

        // Doc2
        assertEquals(doc2.getName(), results.get(1).getName());
        assertEquals("PERS2", results.get(1).getOwner().getName());
        assertEquals(Integer.valueOf(2), results.get(1).getContactPersonNumber());
        assertEquals(Integer.valueOf(2), results.get(1).getTheContactPersonNumber());
        // Filtered subview
        assertEquals(doc2.getContacts().get(2).getName(), results.get(1).getMyContactPerson().getName());
        assertEquals(Integer.valueOf(2), results.get(1).getMyContactPerson().getContactPersonNumber());

        assertSubviewEquals(doc2, doc2.getContacts2(), results.get(1).getContacts());
        assertSubviewEquals(doc2.getPartners(), results.get(1).getPartners());
        assertSubviewEquals(doc2.getPeople(), results.get(1).getPeople());
    }

    public static void assertSubviewEquals(Document doc, Map<Integer, Person> persons, Map<Integer, PersonSubView> personSubviews) {
        if (persons == null) {
            assertNull(personSubviews);
            return;
        }

        assertNotNull(personSubviews);
        assertEquals(persons.size(), personSubviews.size());
        for (Map.Entry<Integer, Person> personEntry : persons.entrySet()) {
            Person p = personEntry.getValue();
            PersonSubView pSub = personSubviews.get(personEntry.getKey());
            assertEquals(p.getName().toUpperCase(), pSub.getName());
            assertEquals(doc.getId(), pSub.getParent().getId());
            assertEquals(doc.getName(), pSub.getParent().getName());
        }
    }

    public static void assertSubviewEquals(List<Person> persons, List<PersonSubView> personSubviews) {
        if (persons == null) {
            assertNull(personSubviews);
            return;
        }

        assertNotNull(personSubviews);
        assertEquals(persons.size(), personSubviews.size());
        for (int i = 0; i < persons.size(); i++) {
            Person p = persons.get(i);
            PersonSubView pSub = personSubviews.get(i);
            assertEquals(p.getName().toUpperCase(), pSub.getName());
        }
    }

    public static void assertSubviewEquals(Set<Person> persons, Set<PersonSubView> personSubviews) {
        if (persons == null) {
            assertNull(personSubviews);
            return;
        }

        assertNotNull(personSubviews);
        assertEquals(persons.size(), personSubviews.size());
        for (Person p : persons) {
            boolean found = false;
            for (PersonSubView pSub : personSubviews) {
                if (p.getName().toUpperCase().equals(pSub.getName())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                Assert.fail("Could not find a person subview instance with the name: " + p.getName());
            }
        }
    }
}
