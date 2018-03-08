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

package com.blazebit.persistence.view.testsuite.predicated.basic;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForCollections;
import com.blazebit.persistence.view.testsuite.predicated.basic.model.BasicPredicatedDocumentCollectionsView;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class BasicPredicatedTest extends AbstractEntityViewTest {

    private DocumentForCollections doc1;
    private DocumentForCollections doc2;

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            DocumentForCollections.class,
            PersonForCollections.class
        };
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                doc1 = new DocumentForCollections("doc1");
                doc2 = new DocumentForCollections("doc2");

                PersonForCollections o1 = new PersonForCollections("pers1");
                PersonForCollections o2 = new PersonForCollections("pers2");
                PersonForCollections o3 = new PersonForCollections("pers3");
                PersonForCollections o4 = new PersonForCollections("pers4");
                o1.setPartnerDocument(doc1);
                o2.setPartnerDocument(doc2);
                o3.setPartnerDocument(doc1);
                o4.setPartnerDocument(doc2);

                doc1.setOwner(o1);
                doc2.setOwner(o2);

                doc1.getContacts().put(0, o1);
                doc2.getContacts().put(0, o2);
                doc1.getContacts().put(1, o3);
                doc2.getContacts().put(1, o4);

                em.persist(o1);
                em.persist(o2);
                em.persist(o3);
                em.persist(o4);

                doc1.getPartners().add(o1);
                doc1.getPartners().add(o3);
                doc2.getPartners().add(o2);
                doc2.getPartners().add(o4);

                doc1.getPersonList().add(o1);
                doc1.getPersonList().add(o2);
                doc2.getPersonList().add(o3);
                doc2.getPersonList().add(o4);

                em.persist(doc1);
                em.persist(doc2);
            }
        });
    }

    @Before
    public void setUp() {
        // Apparently EclipseLink and DataNucleus aren't smart enough to figure out
        // there is only a single entity so we have to use getResultList().get(0) instead of getSingleResult()..
        doc1 = cbf.create(em, DocumentForCollections.class).where("name").eq("doc1")
                .fetch("contacts", "personList")
                .getResultList()
                .get(0);
        doc2 = cbf.create(em, DocumentForCollections.class).where("name").eq("doc2")
                .fetch("contacts", "personList")
                .getResultList()
                .get(0);
    }

    @Test
    // NOTE: DataNucleus renders joins wrong: https://github.com/datanucleus/datanucleus-rdbms/issues/177
    // Apparently EclipseLink just ignores any ON conditions when having join tables...
    @Category({ NoEclipselink.class, NoDatanucleus.class })
    public void multipleBasicPredicatedCollectionsAreFetchedCorrectly() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(BasicPredicatedDocumentCollectionsView.class);
        EntityViewManager evm = cfg.createEntityViewManager(cbf);

        CriteriaBuilder<DocumentForCollections> criteria = cbf.create(em, DocumentForCollections.class, "d")
                .orderByAsc("id");
        CriteriaBuilder<BasicPredicatedDocumentCollectionsView> cb = evm.applySetting(EntityViewSetting.create(BasicPredicatedDocumentCollectionsView.class), criteria);
        List<BasicPredicatedDocumentCollectionsView> results = cb.getResultList();

        assertEquals(2, results.size());
        // Doc1
        assertEquals(doc1.getName(), results.get(0).getName());
        assertEquals(doc1.getContacts().get(0).getName(), results.get(0).getDefaultContactName());
        assertEquals(doc1.getContacts().get(1).getName(), results.get(0).getSecondContactName());
        assertEquals(doc1.getPersonList().get(0).getName(), results.get(0).getDefaultPersonName());
        assertEquals(doc1.getPersonList().get(1).getName(), results.get(0).getSecondPersonName());

        // Doc2
        assertEquals(doc2.getName(), results.get(1).getName());
        assertEquals(doc2.getContacts().get(0).getName(), results.get(1).getDefaultContactName());
        assertEquals(doc2.getContacts().get(1).getName(), results.get(1).getSecondContactName());
        assertEquals(doc2.getPersonList().get(0).getName(), results.get(1).getDefaultPersonName());
        assertEquals(doc2.getPersonList().get(1).getName(), results.get(1).getSecondPersonName());
    }
}
