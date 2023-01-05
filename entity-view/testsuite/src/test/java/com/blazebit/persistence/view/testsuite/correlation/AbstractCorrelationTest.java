/*
 * Copyright 2014 - 2023 Blazebit.
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
import com.blazebit.persistence.view.ConfigurationProperties;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.correlation.model.DocumentCorrelationView;
import com.blazebit.persistence.view.testsuite.correlation.model.SimpleDocumentCorrelatedView;
import com.blazebit.persistence.view.testsuite.correlation.model.SimplePersonCorrelatedSubView;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.testsuite.correlation.model.SimpleVersionCorrelatedView;
import org.junit.Assert;
import org.junit.Before;

import javax.persistence.EntityManager;
import java.util.Collection;
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
        EntityViewManager evm = build(
                entityView,
                SimpleDocumentCorrelatedView.class,
                SimplePersonCorrelatedSubView.class,
                SimpleVersionCorrelatedView.class
        );

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
        assertExistsByName(doc1.getName(), results.get(0).getOwnerOnlyRelatedDocumentViews());
        assertEquals(1, results.get(0).getOwnerOnlyRelatedDocumentIds().size());
        assertExists(doc1.getId(), results.get(0).getOwnerOnlyRelatedDocumentIds());
        assertEquals(0, results.get(0).getThisCorrelatedEmptyIdList().size());
        assertEquals(0, results.get(0).getThisCorrelatedEmptyEntityList().size());
        assertEquals(0, results.get(0).getThisCorrelatedEmptyViewList().size());

        // Doc2
        assertEquals(doc2.getName(), results.get(1).getName());
        assertThisAndOwnerMappings(doc2, results.get(1));

        assertEquals(2, results.get(1).getOwnerRelatedDocumentViews().size());
        assertExistsByName(doc3.getName(), results.get(1).getOwnerRelatedDocumentViews());
        assertExistsByName(doc4.getName(), results.get(1).getOwnerRelatedDocumentViews());
        assertEquals(2, results.get(1).getOwnerRelatedDocumentIds().size());
        assertExists(doc3.getId(), results.get(1).getOwnerRelatedDocumentIds());
        assertExists(doc4.getId(), results.get(1).getOwnerRelatedDocumentIds());

        assertEquals(3, results.get(1).getOwnerOnlyRelatedDocumentViews().size());
        assertExistsByName(doc2.getName(), results.get(1).getOwnerOnlyRelatedDocumentViews());
        assertExistsByName(doc3.getName(), results.get(1).getOwnerOnlyRelatedDocumentViews());
        assertExistsByName(doc4.getName(), results.get(1).getOwnerOnlyRelatedDocumentViews());
        assertEquals(3, results.get(1).getOwnerOnlyRelatedDocumentIds().size());
        assertExists(doc2.getId(), results.get(1).getOwnerOnlyRelatedDocumentIds());
        assertExists(doc3.getId(), results.get(1).getOwnerOnlyRelatedDocumentIds());
        assertExists(doc4.getId(), results.get(1).getOwnerOnlyRelatedDocumentIds());
        assertEquals(0, results.get(1).getThisCorrelatedEmptyIdList().size());
        assertEquals(0, results.get(1).getThisCorrelatedEmptyEntityList().size());
        assertEquals(0, results.get(1).getThisCorrelatedEmptyViewList().size());

        // Doc3
        assertEquals(doc3.getName(), results.get(2).getName());
        assertThisAndOwnerMappings(doc3, results.get(2));

        assertEquals(2, results.get(2).getOwnerRelatedDocumentViews().size());
        assertExistsByName(doc2.getName(), results.get(2).getOwnerRelatedDocumentViews());
        assertExistsByName(doc4.getName(), results.get(2).getOwnerRelatedDocumentViews());
        assertEquals(2, results.get(2).getOwnerRelatedDocumentIds().size());
        assertExists(doc2.getId(), results.get(2).getOwnerRelatedDocumentIds());
        assertExists(doc4.getId(), results.get(2).getOwnerRelatedDocumentIds());

        assertEquals(3, results.get(2).getOwnerOnlyRelatedDocumentViews().size());
        assertExistsByName(doc2.getName(), results.get(2).getOwnerOnlyRelatedDocumentViews());
        assertExistsByName(doc3.getName(), results.get(2).getOwnerOnlyRelatedDocumentViews());
        assertExistsByName(doc4.getName(), results.get(2).getOwnerOnlyRelatedDocumentViews());
        assertEquals(3, results.get(2).getOwnerOnlyRelatedDocumentIds().size());
        assertExists(doc2.getId(), results.get(2).getOwnerOnlyRelatedDocumentIds());
        assertExists(doc3.getId(), results.get(2).getOwnerOnlyRelatedDocumentIds());
        assertExists(doc4.getId(), results.get(2).getOwnerOnlyRelatedDocumentIds());
        assertEquals(0, results.get(2).getThisCorrelatedEmptyIdList().size());
        assertEquals(0, results.get(2).getThisCorrelatedEmptyEntityList().size());
        assertEquals(0, results.get(2).getThisCorrelatedEmptyViewList().size());

        // Doc4
        assertEquals(doc4.getName(), results.get(3).getName());
        assertThisAndOwnerMappings(doc4, results.get(3));

        assertEquals(2, results.get(3).getOwnerRelatedDocumentViews().size());
        assertExistsByName(doc2.getName(), results.get(3).getOwnerRelatedDocumentViews());
        assertExistsByName(doc3.getName(), results.get(3).getOwnerRelatedDocumentViews());
        assertEquals(2, results.get(3).getOwnerRelatedDocumentIds().size());
        assertExists(doc2.getId(), results.get(3).getOwnerRelatedDocumentIds());
        assertExists(doc3.getId(), results.get(3).getOwnerRelatedDocumentIds());

        assertEquals(3, results.get(3).getOwnerOnlyRelatedDocumentViews().size());
        assertExistsByName(doc2.getName(), results.get(3).getOwnerOnlyRelatedDocumentViews());
        assertExistsByName(doc3.getName(), results.get(3).getOwnerOnlyRelatedDocumentViews());
        assertExistsByName(doc4.getName(), results.get(3).getOwnerOnlyRelatedDocumentViews());
        assertEquals(3, results.get(3).getOwnerOnlyRelatedDocumentIds().size());
        assertExists(doc2.getId(), results.get(3).getOwnerOnlyRelatedDocumentIds());
        assertExists(doc3.getId(), results.get(3).getOwnerOnlyRelatedDocumentIds());
        assertExists(doc4.getId(), results.get(3).getOwnerOnlyRelatedDocumentIds());
        assertEquals(0, results.get(3).getThisCorrelatedEmptyIdList().size());
        assertEquals(0, results.get(3).getThisCorrelatedEmptyEntityList().size());
        assertEquals(0, results.get(3).getThisCorrelatedEmptyViewList().size());
    }

    private void assertExistsByName(String expectedName, Collection<SimpleDocumentCorrelatedView> views) {
        for (SimpleDocumentCorrelatedView v : views) {
            if (expectedName.equals(v.getName())) {
                return;
            }
        }

        Assert.fail("Could not find '" + expectedName + "' in: " + views);
    }

    private <T> void assertExists(T expectedValue, Collection<T> collection) {
        if (!collection.contains(expectedValue)) {
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
