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

package com.blazebit.persistence.view.testsuite.fetch.normal;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.impl.ConfigurationProperties;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.fetch.normal.model.DocumentFetchView;
import com.blazebit.persistence.view.testsuite.fetch.normal.model.SimpleDocumentFetchView;
import com.blazebit.persistence.view.testsuite.fetch.normal.model.SimplePersonFetchSubView;
import org.junit.Before;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractFetchTest extends AbstractEntityViewTest {

    protected Document doc1;
    protected Document doc2;
    protected Document doc3;
    protected Document doc4;

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
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

            doc1.getStrings().add("s1");
            doc1.getStrings().add("s2");
            doc2.getStrings().add("s1");
            doc2.getStrings().add("s2");
            doc3.getStrings().add("s1");
            doc3.getStrings().add("s2");
            doc4.getStrings().add("s1");
            doc4.getStrings().add("s2");

            em.persist(o1);
            em.persist(o2);
            em.persist(o3);

            em.persist(doc1);
            em.persist(doc2);
            em.persist(doc3);
            em.persist(doc4);

            o1.setPartnerDocument(doc1);
            o2.setPartnerDocument(doc2);
            o3.setPartnerDocument(doc3);
            }
        });
    }

    @Before
    public void setUp() {
        doc1 = cbf.create(em, Document.class).where("name").eq("doc1").fetch("partners").getResultList().get(0);
        doc2 = cbf.create(em, Document.class).where("name").eq("doc2").fetch("partners").getResultList().get(0);
        doc3 = cbf.create(em, Document.class).where("name").eq("doc3").fetch("partners").getResultList().get(0);
        doc4 = cbf.create(em, Document.class).where("name").eq("doc4").fetch("partners").getResultList().get(0);
    }

    protected <T extends DocumentFetchView> void testCorrelation(Class<T> entityView) {
        testCorrelation(entityView, null);
    }

    protected <T extends DocumentFetchView> void testCorrelation(Class<T> entityView, Integer batchSize) {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(entityView);
        cfg.addEntityView(SimpleDocumentFetchView.class);
        cfg.addEntityView(SimplePersonFetchSubView.class);
        EntityViewManager evm = cfg.createEntityViewManager(cbf);

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d").orderByAsc("id");
        EntityViewSetting<T, CriteriaBuilder<T>> setting = EntityViewSetting.create(entityView);
        if (batchSize != null) {
            setting.setProperty(ConfigurationProperties.DEFAULT_BATCH_SIZE + ".correlatedOwnerId", batchSize);
        }
        CriteriaBuilder<T> cb = evm.applySetting(setting, criteria);
        List<T> results = cb.getResultList();

        assertEquals(4, results.size());

        // Doc1
        assertEquals(doc1.getName(), results.get(0).getName());
        assertThisAndOwnerMappings(doc1, results.get(0));

        // Doc2
        assertEquals(doc2.getName(), results.get(1).getName());
        assertThisAndOwnerMappings(doc2, results.get(1));

        // Doc3
        assertEquals(doc3.getName(), results.get(2).getName());
        assertThisAndOwnerMappings(doc3, results.get(2));

        // Doc4
        assertEquals(doc4.getName(), results.get(3).getName());
        assertThisAndOwnerMappings(doc4, results.get(3));
    }

    private void assertThisAndOwnerMappings(Document doc, DocumentFetchView view) {
        // ThisCorrelated variants
        assertEquals(doc, view.getThisCorrelatedEntity());
        assertEquals(doc.getId(), view.getThisCorrelatedId());
        assertDocumentEqualsView(doc, view.getThisCorrelatedView());

        assertEquals(1, view.getThisCorrelatedEntityList().size());
        assertEquals(doc, view.getThisCorrelatedEntityList().iterator().next());

        assertEquals(1, view.getThisCorrelatedIdList().size());
        assertEquals(doc.getId(), view.getThisCorrelatedIdList().iterator().next());

        assertEquals(1, view.getThisCorrelatedViewList().size());
        assertDocumentEqualsView(doc, view.getThisCorrelatedViewList().iterator().next());

        // CorrelatedOwner variants
        assertEquals(doc.getOwner(), view.getCorrelatedOwner());
        assertEquals(doc.getOwner().getId(), view.getCorrelatedOwnerId());
        assertDocumentEqualsView(doc.getOwner(), view.getCorrelatedOwnerView());

        assertEquals(1, view.getCorrelatedOwnerList().size());
        assertEquals(doc.getOwner(), view.getCorrelatedOwnerList().iterator().next());

        assertEquals(1, view.getCorrelatedOwnerIdList().size());
        assertEquals(doc.getOwner().getId(), view.getCorrelatedOwnerIdList().iterator().next());

        assertEquals(1, view.getCorrelatedOwnerViewList().size());
        assertDocumentEqualsView(doc.getOwner(), view.getCorrelatedOwnerViewList().iterator().next());

        // Partners variants
        assertEquals(doc.getPartners().size(), view.getPartnerIdList().size());
        assertEquals(doc.getPartners().size(), view.getPartnerList().size());
        assertEquals(doc.getPartners().size(), view.getPartnerViewList().size());

        for (Person person : doc.getPartners()) {
            assertTrue(view.getPartnerIdList().contains(person.getId()));
            assertTrue(view.getPartnerList().contains(person));
            SimplePersonFetchSubView partnerView = null;
            for (SimplePersonFetchSubView v : view.getPartnerViewList()) {
                if (v.getId().equals(person.getId())) {
                    partnerView = v;
                    break;
                }
            }

            assertNotNull(partnerView);
            assertEquals(person.getName().toUpperCase(), partnerView.getName());
        }
    }

    private void assertDocumentEqualsView(Document doc, SimpleDocumentFetchView view) {
        assertEquals(doc.getId(), view.getId());
        assertEquals(doc.getName(), view.getName());
        assertDocumentEqualsView(doc.getOwner(), view.getOwner());
    }

    private void assertDocumentEqualsView(Person pers, SimplePersonFetchSubView view) {
        assertEquals(pers.getId(), view.getId());
        assertEquals(pers.getName().toUpperCase(), view.getName());
    }
}
