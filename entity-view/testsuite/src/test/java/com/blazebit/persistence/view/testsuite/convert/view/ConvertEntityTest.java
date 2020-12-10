/*
 * Copyright 2014 - 2020 Blazebit.
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

import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.ConvertOption;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.convert.view.model.CreatablePersonView;
import com.blazebit.persistence.view.testsuite.convert.view.model.CreatablePersonView2;
import com.blazebit.persistence.view.testsuite.convert.view.model.DocumentCloneUpdateView;
import com.blazebit.persistence.view.testsuite.convert.view.model.DocumentCloneView;
import com.blazebit.persistence.view.testsuite.convert.view.model.DocumentCloneView2;
import com.blazebit.persistence.view.testsuite.convert.view.model.DocumentIdView;
import com.blazebit.persistence.view.testsuite.convert.view.model.PersonView;
import com.blazebit.persistence.view.testsuite.convert.view.model.SimplePersonView;
import com.blazebit.persistence.view.testsuite.convert.view.model.sub.DocumentCloneParentView;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
// NOTE: EclipseLink can't handle selecting a map..
@Category({ NoEclipselink.class })
public class ConvertEntityTest extends AbstractEntityViewTest {

    protected EntityViewManager evm;

    @Before
    public void initEvm() {
        evm = build(
                DocumentIdView.class,
                DocumentCloneView.class,
                DocumentCloneView2.class,
                DocumentCloneParentView.class,
                DocumentCloneUpdateView.class,
                SimplePersonView.class,
                CreatablePersonView.class,
                CreatablePersonView2.class,
                PersonView.class
        );
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
        DocumentCloneView clone = evm.convert(doc, DocumentCloneView.class);
        PersonView ownerView = evm.convert(doc.getOwner(), PersonView.class);

        assertEquals(doc.getId(), clone.getId());
        assertEquals(doc.getAge(), clone.getAge());
        assertEquals(doc.getName(), clone.getName());
        assertEquals(doc.getContacts(), clone.getContacts());
        assertEquals(ownerView, clone.getOwner());
        assertEquals(doc.getOwner().getName(), clone.getOwner().getName());
        assertEquals(doc.getOwner().getFriend().getName(), clone.getOwner().getFriend().getName());
        assertEquals(doc.getPeople().get(0).getName(), clone.getPeople().get(0).getName());
        assertEquals(doc.getPeople().get(0).getFriend().getName(), clone.getPeople().get(0).getFriend().getName());
        assertEquals(doc.getPartners().iterator().next().getName(), clone.getPartners().iterator().next().getName());
        assertEquals(doc.getPartners().iterator().next().getFriend().getName(), clone.getPartners().iterator().next().getFriend().getName());
        assertTrue(doc == clone.getSource());
    }

    @Test
    public void testCloneConvertWithBuilder() {
        DocumentCloneView clone = evm.convertWith(doc, DocumentCloneView.class)
                .convertAttribute("owner.friend", CreatablePersonView.class, ConvertOption.CREATE_NEW)
                .excludeAttribute("people")
                .convertAttribute("partners", CreatablePersonView2.class, ConvertOption.CREATE_NEW)
                .excludeAttribute("partners.id")
                .convert();
        PersonView ownerView = evm.convert(doc.getOwner(), PersonView.class);
        

        assertEquals(doc.getId(), clone.getId());
        assertEquals(doc.getAge(), clone.getAge());
        assertEquals(doc.getName(), clone.getName());
        assertEquals(doc.getContacts(), clone.getContacts());
        assertEquals(ownerView, clone.getOwner());
        assertEquals(doc.getOwner().getName(), clone.getOwner().getName());
        assertEquals(doc.getOwner().getFriend().getName(), clone.getOwner().getFriend().getName());
        assertNull(clone.getPartners().iterator().next().getId());
        assertEquals(doc.getPartners().iterator().next().getName(), clone.getPartners().iterator().next().getName());
        assertNull(clone.getPartners().iterator().next().getFriend().getId());
        assertEquals(doc.getPartners().iterator().next().getFriend().getName(), clone.getPartners().iterator().next().getFriend().getName());

        assertNull(clone.getPeople());
        assertTrue(((EntityViewProxy) clone.getOwner().getFriend()).$$_isNew());
        assertTrue(clone.getOwner().getFriend() instanceof CreatablePersonView);
        assertTrue(doc == clone.getSource());
    }

    @Test
    public void testConvertSubset() {
        DocumentIdView idView = evm.convert(doc, DocumentIdView.class);
        PersonView ownerView = evm.convert(doc.getOwner(), PersonView.class);

        assertEquals(doc.getId(), idView.getId());
        assertEquals(ownerView, idView.getOwner());
        assertEquals(doc.getOwner().getName(), idView.getOwner().getName());
    }

    @Test
    public void testCloneConvertFromOtherEntity() {
        try {
            evm.convert(doc.getOwner(), DocumentCloneView.class);
            Assert.fail("Expected validation exception!");
        } catch (IllegalArgumentException ex) {
            if (!ex.getMessage().contains("is not an instance of the target")) {
                throw ex;
            }
        }
    }
}
