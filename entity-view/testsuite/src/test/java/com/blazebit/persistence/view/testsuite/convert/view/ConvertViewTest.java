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

package com.blazebit.persistence.view.testsuite.convert.view;

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
import com.blazebit.persistence.view.testsuite.convert.view.model.DocumentCloneView;
import com.blazebit.persistence.view.testsuite.convert.view.model.DocumentIdView;
import com.blazebit.persistence.view.testsuite.convert.view.model.PersonView;
import com.blazebit.persistence.view.testsuite.convert.view.model.SimplePersonView;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
// NOTE: EclipseLink can't handle selecting a map..
@Category({ NoEclipselink.class })
public class ConvertViewTest extends AbstractEntityViewTest {

    protected EntityViewManager evm;

    @Before
    public void initEvm() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(DocumentIdView.class);
        cfg.addEntityView(DocumentCloneView.class);
        cfg.addEntityView(SimplePersonView.class);
        cfg.addEntityView(PersonView.class);
        evm = cfg.createEntityViewManager(cbf);
    }

    private Document doc;

    @Before
    public void setUp() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                doc = new Document("doc1", 1);
                Person pers1 = new Person("pers1");
                Person pers2 = new Person("pers2");

                em.persist(pers1);
                em.persist(pers2);

                pers1.setFriend(pers2);
                pers2.setFriend(pers1);

                doc.setOwner(pers1);
                doc.getPeople().add(pers1);
                doc.getContacts().put(1, pers1);
                doc.getContacts().put(2, pers2);
                em.persist(doc);
                pers2.setPartnerDocument(doc);
            }
        });
        doc = cbf.create(em, Document.class).fetch("people", "contacts.friend", "partners").getResultList().get(0);
    }

    @Test
    public void testCloneConvert() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class);
        DocumentCloneView documentView = evm.applySetting(EntityViewSetting.create(DocumentCloneView.class), criteria)
            .getSingleResult();
        DocumentCloneView clone = evm.convert(documentView, DocumentCloneView.class);

        assertEquals(documentView.getId(), clone.getId());
        assertEquals(documentView.getAge(), clone.getAge());
        assertEquals(documentView.getName(), clone.getName());
        assertEquals(documentView.getContacts(), clone.getContacts());
        assertEquals(documentView.getOwner(), clone.getOwner());
        assertEquals(documentView.getOwner().getName(), clone.getOwner().getName());
        assertEquals(documentView.getOwner().getFriend(), clone.getOwner().getFriend());
        assertEquals(documentView.getOwner().getFriend().getName(), clone.getOwner().getFriend().getName());
        assertEquals(documentView.getPeople(), clone.getPeople());
        assertEquals(documentView.getPeople().get(0).getName(), clone.getPeople().get(0).getName());
        assertEquals(documentView.getPeople().get(0).getFriend(), clone.getPeople().get(0).getFriend());
        assertEquals(documentView.getPeople().get(0).getFriend().getName(), clone.getPeople().get(0).getFriend().getName());
        assertEquals(documentView.getPartners(), clone.getPartners());
        assertEquals(documentView.getPartners().iterator().next().getName(), clone.getPartners().iterator().next().getName());
        assertEquals(documentView.getPartners().iterator().next().getFriend(), clone.getPartners().iterator().next().getFriend());
        assertEquals(documentView.getPartners().iterator().next().getFriend().getName(), clone.getPartners().iterator().next().getFriend().getName());
    }

    @Test
    public void testConvertSubset() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class);
        DocumentCloneView documentView = evm.applySetting(EntityViewSetting.create(DocumentCloneView.class), criteria)
                .getSingleResult();
        DocumentIdView idView = evm.convert(documentView, DocumentIdView.class);

        assertEquals(documentView.getId(), idView.getId());
        assertEquals(documentView.getOwner(), idView.getOwner());
        assertEquals(documentView.getOwner().getName(), idView.getOwner().getName());
    }
}
