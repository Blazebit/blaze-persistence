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

package com.blazebit.persistence.view.testsuite.correlation;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.impl.ConfigurationProperties;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.correlation.model.DocumentCorrelationView;
import com.blazebit.persistence.view.testsuite.correlation.model.SimpleDocumentCorrelatedView;
import com.blazebit.persistence.view.testsuite.correlation.model.SimplePersonCorrelatedSubView;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import org.junit.Assert;
import org.junit.Before;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractCorrelationTest extends AbstractEntityViewTest {

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

            em.persist(o1);
            em.persist(o2);
            em.persist(o3);

            em.persist(doc1);
            em.persist(doc2);
            em.persist(doc3);
            em.persist(doc4);
            }
        });
    }

    @Before
    public void setUp() {
        doc1 = cbf.create(em, Document.class).where("name").eq("doc1").getSingleResult();
        doc2 = cbf.create(em, Document.class).where("name").eq("doc2").getSingleResult();
        doc3 = cbf.create(em, Document.class).where("name").eq("doc3").getSingleResult();
        doc4 = cbf.create(em, Document.class).where("name").eq("doc4").getSingleResult();
    }

    protected <T extends DocumentCorrelationView> void testCorrelation(Class<T> entityView, Integer batchSize) {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(entityView);
        cfg.addEntityView(SimpleDocumentCorrelatedView.class);
        cfg.addEntityView(SimplePersonCorrelatedSubView.class);
        EntityViewManager evm = cfg.createEntityViewManager(cbf);

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d").orderByAsc("id");
        EntityViewSetting<T, CriteriaBuilder<T>> setting = EntityViewSetting.create(entityView);
        if (batchSize != null) {
            setting.setProperty(ConfigurationProperties.DEFAULT_BATCH_SIZE + ".ownerRelatedDocumentIds", batchSize);
        }
        CriteriaBuilder<T> cb = evm.applySetting(setting, criteria);
        List<T> results = cb.getResultList();

        assertEquals(4, results.size());

        // Doc1
        assertEquals(doc1.getName(), results.get(0).getName());
        assertThisAndOwnerMappings(doc1, results.get(0));

        assertEquals(0, results.get(0).getOwnerRelatedDocumentViews().size());
        assertEquals(0, results.get(0).getOwnerRelatedDocumentIds().size());

        assertEquals(1, results.get(0).getOwnerOnlyRelatedDocumentViews().size());
        assertRemovedByName(doc1.getName(), results.get(0).getOwnerOnlyRelatedDocumentViews());
        assertEquals(1, results.get(0).getOwnerOnlyRelatedDocumentIds().size());
        assertRemoved(doc1.getId(), results.get(0).getOwnerOnlyRelatedDocumentIds());

        // Doc2
        assertEquals(doc2.getName(), results.get(1).getName());
        assertThisAndOwnerMappings(doc2, results.get(1));

        assertEquals(2, results.get(1).getOwnerRelatedDocumentViews().size());
        assertRemovedByName(doc3.getName(), results.get(1).getOwnerRelatedDocumentViews());
        assertRemovedByName(doc4.getName(), results.get(1).getOwnerRelatedDocumentViews());
        assertEquals(2, results.get(1).getOwnerRelatedDocumentIds().size());
        assertRemoved(doc3.getId(), results.get(1).getOwnerRelatedDocumentIds());
        assertRemoved(doc4.getId(), results.get(1).getOwnerRelatedDocumentIds());

        assertEquals(3, results.get(1).getOwnerOnlyRelatedDocumentViews().size());
        assertRemovedByName(doc2.getName(), results.get(1).getOwnerOnlyRelatedDocumentViews());
        assertRemovedByName(doc3.getName(), results.get(1).getOwnerOnlyRelatedDocumentViews());
        assertRemovedByName(doc4.getName(), results.get(1).getOwnerOnlyRelatedDocumentViews());
        assertEquals(3, results.get(1).getOwnerOnlyRelatedDocumentIds().size());
        assertRemoved(doc2.getId(), results.get(1).getOwnerOnlyRelatedDocumentIds());
        assertRemoved(doc3.getId(), results.get(1).getOwnerOnlyRelatedDocumentIds());
        assertRemoved(doc4.getId(), results.get(1).getOwnerOnlyRelatedDocumentIds());

        // Doc3
        assertEquals(doc3.getName(), results.get(2).getName());
        assertThisAndOwnerMappings(doc3, results.get(2));

        assertEquals(2, results.get(2).getOwnerRelatedDocumentViews().size());
        assertRemovedByName(doc2.getName(), results.get(2).getOwnerRelatedDocumentViews());
        assertRemovedByName(doc4.getName(), results.get(2).getOwnerRelatedDocumentViews());
        assertEquals(2, results.get(2).getOwnerRelatedDocumentIds().size());
        assertRemoved(doc2.getId(), results.get(2).getOwnerRelatedDocumentIds());
        assertRemoved(doc4.getId(), results.get(2).getOwnerRelatedDocumentIds());

        assertEquals(3, results.get(2).getOwnerOnlyRelatedDocumentViews().size());
        assertRemovedByName(doc2.getName(), results.get(2).getOwnerOnlyRelatedDocumentViews());
        assertRemovedByName(doc3.getName(), results.get(2).getOwnerOnlyRelatedDocumentViews());
        assertRemovedByName(doc4.getName(), results.get(2).getOwnerOnlyRelatedDocumentViews());
        assertEquals(3, results.get(2).getOwnerOnlyRelatedDocumentIds().size());
        assertRemoved(doc2.getId(), results.get(2).getOwnerOnlyRelatedDocumentIds());
        assertRemoved(doc3.getId(), results.get(2).getOwnerOnlyRelatedDocumentIds());
        assertRemoved(doc4.getId(), results.get(2).getOwnerOnlyRelatedDocumentIds());

        // Doc4
        assertEquals(doc4.getName(), results.get(3).getName());
        assertThisAndOwnerMappings(doc4, results.get(3));

        assertEquals(2, results.get(3).getOwnerRelatedDocumentViews().size());
        assertRemovedByName(doc2.getName(), results.get(3).getOwnerRelatedDocumentViews());
        assertRemovedByName(doc3.getName(), results.get(3).getOwnerRelatedDocumentViews());
        assertEquals(2, results.get(3).getOwnerRelatedDocumentIds().size());
        assertRemoved(doc2.getId(), results.get(3).getOwnerRelatedDocumentIds());
        assertRemoved(doc3.getId(), results.get(3).getOwnerRelatedDocumentIds());

        assertEquals(3, results.get(3).getOwnerOnlyRelatedDocumentViews().size());
        assertRemovedByName(doc2.getName(), results.get(3).getOwnerOnlyRelatedDocumentViews());
        assertRemovedByName(doc3.getName(), results.get(3).getOwnerOnlyRelatedDocumentViews());
        assertRemovedByName(doc4.getName(), results.get(3).getOwnerOnlyRelatedDocumentViews());
        assertEquals(3, results.get(3).getOwnerOnlyRelatedDocumentIds().size());
        assertRemoved(doc2.getId(), results.get(3).getOwnerOnlyRelatedDocumentIds());
        assertRemoved(doc3.getId(), results.get(3).getOwnerOnlyRelatedDocumentIds());
        assertRemoved(doc4.getId(), results.get(3).getOwnerOnlyRelatedDocumentIds());
    }

    private void assertRemovedByName(String expectedName, Collection<SimpleDocumentCorrelatedView> views) {
        Iterator<SimpleDocumentCorrelatedView> iter = views.iterator();
        while (iter.hasNext()) {
            SimpleDocumentCorrelatedView v = iter.next();
            if (expectedName.equals(v.getName())) {
                iter.remove();
                return;
            }
        }

        Assert.fail("Could not find '" + expectedName + "' in: " + views);
    }

    private <T> void assertRemoved(T expectedValue, Collection<T> collection) {
        if (!collection.remove(expectedValue)) {
            Assert.fail("Could not find '" + expectedValue + "' in: " + collection);
        }
    }

    private void assertThisAndOwnerMappings(Document doc, DocumentCorrelationView view) {
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
    }

    private void assertDocumentEqualsView(Document doc, SimpleDocumentCorrelatedView view) {
        assertEquals(doc.getId(), view.getId());
        assertEquals(doc.getName(), view.getName());
        assertDocumentEqualsView(doc.getOwner(), view.getOwner());
    }

    private void assertDocumentEqualsView(Person pers, SimplePersonCorrelatedSubView view) {
        assertEquals(pers.getId(), view.getId());
        assertEquals(pers.getName().toUpperCase(), view.getName());
    }
}
