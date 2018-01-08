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

package com.blazebit.persistence.view.testsuite.inheritance.constructor;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.testsuite.inheritance.constructor.model.DocumentBaseView;
import com.blazebit.persistence.view.testsuite.inheritance.constructor.model.NewDocumentView;
import com.blazebit.persistence.view.testsuite.inheritance.constructor.model.OldDocumentView;
import com.blazebit.persistence.view.testsuite.inheritance.constructor.model.SimplePersonSubView;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ConstructorInheritanceTest extends AbstractEntityViewTest {

    private Document doc1;
    private Document doc2;
    private Document doc3;
    private EntityViewManager evm;

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                doc1 = new Document("doc1", new Person("owner1"));
                doc2 = new Document("doc2", new Person("owner2"));
                doc3 = new Document("doc3", new Person("owner3"));

                Person o1 = new Person("pers1");
                Person o2 = new Person("pers2");
                Person o3 = new Person("pers3");
                Person o4 = new Person("pers4");

                // New
                doc1.setAge(1);
                doc1.getContacts().put(1, o1);
                doc1.getContacts().put(1, o2);

                // Base
                doc2.setAge(15);
                // Old
                doc3.setAge(16);

                em.persist(o1);
                em.persist(o2);
                em.persist(o3);
                em.persist(o4);

                o3.setPartnerDocument(doc3);
                o4.setPartnerDocument(doc3);

                em.persist(doc1);
                em.persist(doc2);
                em.persist(doc3);
            }
        });
    }

    @Before
    public void setUp() {
        doc1 = cbf.create(em, Document.class).where("name").eq("doc1").getSingleResult();
        doc2 = cbf.create(em, Document.class).where("name").eq("doc2").getSingleResult();
        doc3 = cbf.create(em, Document.class).where("name").eq("doc3").getSingleResult();

        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(SimplePersonSubView.class);
        cfg.addEntityView(DocumentBaseView.class);
        cfg.addEntityView(NewDocumentView.class);
        cfg.addEntityView(OldDocumentView.class);
        this.evm = cfg.createEntityViewManager(cbf);
    }

    @Test
    public void inheritanceQuery() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d")
            .orderByAsc("name");
        CriteriaBuilder<DocumentBaseView> cb = evm.applySetting(EntityViewSetting.create(DocumentBaseView.class), criteria);
        List<DocumentBaseView> results = cb.getResultList();

        assertEquals(3, results.size());

        NewDocumentView docView1 = (NewDocumentView) results.get(0);
        DocumentBaseView docView2 = (DocumentBaseView) results.get(1);
        OldDocumentView docView3 = (OldDocumentView) results.get(2);

        assertDocumentEquals(doc1, docView1);
        assertDocumentEquals(doc2, docView2);
        assertDocumentEquals(doc3, docView3);

        assertSubviewEquals(doc1.getContacts().values(), docView1.getPeople());
        assertSubviewEquals(Collections.singleton(doc2.getOwner()), docView2.getPeople());
        assertSubviewEquals(doc3.getPartners(), docView3.getPeople());
    }

    public static void assertDocumentEquals(Document doc, DocumentBaseView view) {
        assertEquals(doc.getId(), view.getId());
        assertEquals(doc.getName(), view.getName());
    }

    public static void assertSubviewEquals(Collection<Person> persons, Collection<SimplePersonSubView> personSubviews) {
        if (persons == null) {
            assertNull(personSubviews);
            return;
        }

        assertNotNull(personSubviews);
        assertEquals(persons.size(), personSubviews.size());
        for (Person p : persons) {
            boolean found = false;
            for (SimplePersonSubView pSub : personSubviews) {
                if (p.getName().equals(pSub.getName())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                Assert.fail("Could not find a SimplePersonSubView with the name: " + p.getName());
            }
        }
    }
}
