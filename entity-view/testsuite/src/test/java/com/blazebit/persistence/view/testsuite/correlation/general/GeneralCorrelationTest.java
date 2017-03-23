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

package com.blazebit.persistence.view.testsuite.correlation.general;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.category.*;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.impl.ConfigurationProperties;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.correlation.general.model.DocumentCorrelationView;
import com.blazebit.persistence.view.testsuite.correlation.general.model.DocumentCorrelationViewJoinId;
import com.blazebit.persistence.view.testsuite.correlation.general.model.DocumentCorrelationViewJoinNormal;
import com.blazebit.persistence.view.testsuite.correlation.general.model.DocumentCorrelationViewSubqueryId;
import com.blazebit.persistence.view.testsuite.correlation.general.model.DocumentCorrelationViewSubqueryNormal;
import com.blazebit.persistence.view.testsuite.correlation.general.model.DocumentCorrelationViewSubselectId;
import com.blazebit.persistence.view.testsuite.correlation.general.model.DocumentCorrelationViewSubselectNormal;
import com.blazebit.persistence.view.testsuite.entity.Document;
import com.blazebit.persistence.view.testsuite.entity.Person;
import com.blazebit.persistence.view.testsuite.subview.model.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
public class GeneralCorrelationTest extends AbstractEntityViewTest {

    private Document doc1;
    private Document doc2;
    private Document doc3;
    private Document doc4;

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

    @Test
    // NOTE: Datenucleus issue: https://github.com/datanucleus/datanucleus-api-jpa/issues/77
    @Category({ NoDatanucleus.class })
    public void testSubqueryCorrelationNormal() {
        testCorrelation(DocumentCorrelationViewSubqueryNormal.class, null);
    }

    @Test
    public void testSubqueryCorrelationId() {
        testCorrelation(DocumentCorrelationViewSubqueryId.class, null);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedCorrelationNormalSize2() {
        testCorrelation(DocumentCorrelationViewSubqueryNormal.class, 2);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedCorrelationIdSize2() {
        testCorrelation(DocumentCorrelationViewSubqueryId.class, 2);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedCorrelationNormalSize4() {
        testCorrelation(DocumentCorrelationViewSubqueryNormal.class, 4);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedCorrelationIdSize4() {
        testCorrelation(DocumentCorrelationViewSubqueryId.class, 4);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedCorrelationNormalSize20() {
        testCorrelation(DocumentCorrelationViewSubqueryNormal.class, 20);
    }

    @Test
    // NOTE: Requires values clause which currently is only available for Hibernate
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
    public void testSubqueryBatchedCorrelationIdSize20() {
        testCorrelation(DocumentCorrelationViewSubqueryId.class, 20);
    }

    // TODO: test batch correlation expectation configuration
    // TODO: make explicit test for correlation key batching with view root usage maybe via nested subviews through collections?

    @Test
    public void testSubselectCorrelationNormal() {
        testCorrelation(DocumentCorrelationViewSubselectNormal.class, null);
    }

    @Test
    public void testSubselectCorrelationId() {
        testCorrelation(DocumentCorrelationViewSubselectId.class, null);
    }

    @Test
    // NOTE: Requires entity joins which are supported since Hibernate 5.1, Datanucleus 5 and latest Eclipselink
    // NOTE: Eclipselink renders a cross join at the wrong position in the SQL
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class, NoOpenJPA.class, NoEclipselink.class })
    public void testJoinCorrelationNormal() {
        testCorrelation(DocumentCorrelationViewJoinNormal.class, null);
    }

    @Test
    // NOTE: Requires entity joins which are supported since Hibernate 5.1, Datanucleus 5 and latest Eclipselink
    // NOTE: Eclipselink renders a cross join at the wrong position in the SQL
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class, NoOpenJPA.class, NoEclipselink.class })
    public void testJoinCorrelationId() {
        testCorrelation(DocumentCorrelationViewJoinId.class, null);
    }

    private <T extends DocumentCorrelationView> void testCorrelation(Class<T> entityView, Integer batchSize) {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(entityView);
        cfg.addEntityView(DocumentRelatedView.class);
        cfg.addEntityView(SimplePersonSubView.class);
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
        assertEquals(0, results.get(0).getOwnerRelatedDocuments().size());
        assertEquals(0, results.get(0).getOwnerRelatedDocumentIds().size());

        assertEquals(1, results.get(0).getOwnerOnlyRelatedDocuments().size());
        assertRemovedByName(doc1.getName(), results.get(0).getOwnerOnlyRelatedDocuments());
        assertEquals(1, results.get(0).getOwnerOnlyRelatedDocumentIds().size());
        assertRemoved(doc1.getId(), results.get(0).getOwnerOnlyRelatedDocumentIds());

        // Doc2
        assertEquals(doc2.getName(), results.get(1).getName());
        assertEquals(2, results.get(1).getOwnerRelatedDocuments().size());
        assertRemovedByName(doc3.getName(), results.get(1).getOwnerRelatedDocuments());
        assertRemovedByName(doc4.getName(), results.get(1).getOwnerRelatedDocuments());
        assertEquals(2, results.get(1).getOwnerRelatedDocumentIds().size());
        assertRemoved(doc3.getId(), results.get(1).getOwnerRelatedDocumentIds());
        assertRemoved(doc4.getId(), results.get(1).getOwnerRelatedDocumentIds());

        assertEquals(3, results.get(1).getOwnerOnlyRelatedDocuments().size());
        assertRemovedByName(doc2.getName(), results.get(1).getOwnerOnlyRelatedDocuments());
        assertRemovedByName(doc3.getName(), results.get(1).getOwnerOnlyRelatedDocuments());
        assertRemovedByName(doc4.getName(), results.get(1).getOwnerOnlyRelatedDocuments());
        assertEquals(3, results.get(1).getOwnerOnlyRelatedDocumentIds().size());
        assertRemoved(doc2.getId(), results.get(1).getOwnerOnlyRelatedDocumentIds());
        assertRemoved(doc3.getId(), results.get(1).getOwnerOnlyRelatedDocumentIds());
        assertRemoved(doc4.getId(), results.get(1).getOwnerOnlyRelatedDocumentIds());

        // Doc3
        assertEquals(doc3.getName(), results.get(2).getName());
        assertEquals(2, results.get(2).getOwnerRelatedDocuments().size());
        assertRemovedByName(doc2.getName(), results.get(2).getOwnerRelatedDocuments());
        assertRemovedByName(doc4.getName(), results.get(2).getOwnerRelatedDocuments());
        assertEquals(2, results.get(2).getOwnerRelatedDocumentIds().size());
        assertRemoved(doc2.getId(), results.get(2).getOwnerRelatedDocumentIds());
        assertRemoved(doc4.getId(), results.get(2).getOwnerRelatedDocumentIds());

        assertEquals(3, results.get(2).getOwnerOnlyRelatedDocuments().size());
        assertRemovedByName(doc2.getName(), results.get(2).getOwnerOnlyRelatedDocuments());
        assertRemovedByName(doc3.getName(), results.get(2).getOwnerOnlyRelatedDocuments());
        assertRemovedByName(doc4.getName(), results.get(2).getOwnerOnlyRelatedDocuments());
        assertEquals(3, results.get(2).getOwnerOnlyRelatedDocumentIds().size());
        assertRemoved(doc2.getId(), results.get(2).getOwnerOnlyRelatedDocumentIds());
        assertRemoved(doc3.getId(), results.get(2).getOwnerOnlyRelatedDocumentIds());
        assertRemoved(doc4.getId(), results.get(2).getOwnerOnlyRelatedDocumentIds());

        // Doc4
        assertEquals(doc4.getName(), results.get(3).getName());
        assertEquals(2, results.get(3).getOwnerRelatedDocuments().size());
        assertRemovedByName(doc2.getName(), results.get(3).getOwnerRelatedDocuments());
        assertRemovedByName(doc3.getName(), results.get(3).getOwnerRelatedDocuments());
        assertEquals(2, results.get(3).getOwnerRelatedDocumentIds().size());
        assertRemoved(doc2.getId(), results.get(3).getOwnerRelatedDocumentIds());
        assertRemoved(doc3.getId(), results.get(3).getOwnerRelatedDocumentIds());

        assertEquals(3, results.get(3).getOwnerOnlyRelatedDocuments().size());
        assertRemovedByName(doc2.getName(), results.get(3).getOwnerOnlyRelatedDocuments());
        assertRemovedByName(doc3.getName(), results.get(3).getOwnerOnlyRelatedDocuments());
        assertRemovedByName(doc4.getName(), results.get(3).getOwnerOnlyRelatedDocuments());
        assertEquals(3, results.get(3).getOwnerOnlyRelatedDocumentIds().size());
        assertRemoved(doc2.getId(), results.get(3).getOwnerOnlyRelatedDocumentIds());
        assertRemoved(doc3.getId(), results.get(3).getOwnerOnlyRelatedDocumentIds());
        assertRemoved(doc4.getId(), results.get(3).getOwnerOnlyRelatedDocumentIds());
    }

    private void assertRemovedByName(String expectedName, Collection<DocumentRelatedView> views) {
        Iterator<DocumentRelatedView> iter = views.iterator();
        while (iter.hasNext()) {
            DocumentRelatedView v = iter.next();
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
}
